package com.ticketsystem.service.impl;

import com.ticketsystem.dto.request.TicketRequest;
import com.ticketsystem.dto.response.*;
import com.ticketsystem.entity.*;
import com.ticketsystem.exception.*;
import com.ticketsystem.repository.*;
import com.ticketsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TicketHistoryRepository historyRepository;
    private final TicketCommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public TicketResponse createTicket(TicketRequest request, String username) {
        User creator = getUserByUsername(username);
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", request.getProjectId()));

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
        }

        String ticketNumber = generateTicketNumber();
        LocalDateTime now = LocalDateTime.now();

        // SLA based on priority
        LocalDateTime slaDue = calculateSlaDueDate(request.getPriority(), now);

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .issueDescription(request.getIssueDescription())
                .project(project)
                .assignedTo(assignedTo)
                .createdBy(creator)
                .supportLevel(request.getSupportLevel())
                .priority(request.getPriority())
                .generationDateTime(now)
                .currentStatus(TicketStatus.OPEN)
                .remarks(request.getRemarks())
                .slaDueDateTime(slaDue)
                .build();

        ticket = ticketRepository.save(ticket);

        // Log creation history
        logHistory(ticket, creator, "CREATED", null, "OPEN", "Ticket created");

        log.info("Ticket {} created by {}", ticketNumber, username);
        return mapToResponse(ticket);
    }

    @Override
    public TicketResponse updateTicket(Long id, TicketRequest request, String username) {
        User updater = getUserByUsername(username);
        Ticket ticket = getTicketEntityById(id);

        // Validate resolution details when resolving/closing
        if (request.getCurrentStatus() != null &&
                (request.getCurrentStatus() == TicketStatus.RESOLVED || request.getCurrentStatus() == TicketStatus.CLOSED)) {
            if (request.getResolutionDetails() == null || request.getResolutionDetails().isBlank()) {
                throw new BadRequestException("Resolution details are required when status is Resolved or Closed");
            }
        }

        String oldStatus = ticket.getCurrentStatus().name();
        TicketStatus oldStatusEnum = ticket.getCurrentStatus();

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", request.getProjectId()));
            ticket.setProject(project);
        }
        if (request.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            if (!Objects.equals(ticket.getAssignedTo(), assignedTo)) {
                logHistory(ticket, updater, "assignedTo",
                        ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : "Unassigned",
                        assignedTo.getFullName(), "Ticket reassigned");
                ticket.setAssignedTo(assignedTo);
            }
        }

        if (request.getSupportLevel() != null) ticket.setSupportLevel(request.getSupportLevel());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        if (request.getIssueDescription() != null) ticket.setIssueDescription(request.getIssueDescription());
        if (request.getRemarks() != null) ticket.setRemarks(request.getRemarks());

        // Status transition
        if (request.getCurrentStatus() != null && request.getCurrentStatus() != oldStatusEnum) {
            ticket.setCurrentStatus(request.getCurrentStatus());
            logHistory(ticket, updater, "currentStatus", oldStatus, request.getCurrentStatus().name(), "Status updated");

            if (ticket.getResponseDateTime() == null) {
                ticket.setResponseDateTime(LocalDateTime.now());
            }
            if (request.getCurrentStatus() == TicketStatus.RESOLVED || request.getCurrentStatus() == TicketStatus.CLOSED) {
                LocalDateTime now = LocalDateTime.now();
                ticket.setResolutionDateTime(now);
                long minutes = ChronoUnit.MINUTES.between(ticket.getGenerationDateTime(), now);
                ticket.setResolutionTimeMinutes(minutes);
                ticket.setResolutionDetails(request.getResolutionDetails());
            }
        }

        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        return mapToResponse(getTicketEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketNumber));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsWithFilters(
            String ticketNumber, Long projectId, Long assignedToId,
            String priority, String status, String supportLevel,
            LocalDate startDate, LocalDate endDate,
            String search, Pageable pageable) {

        Specification<Ticket> spec = buildSpecification(ticketNumber, projectId, assignedToId,
                priority, status, supportLevel, startDate, endDate, search);
        return ticketRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsByUser(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        return ticketRepository.findByCreatedBy(user, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getTicketsAssignedToUser(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        return ticketRepository.findByAssignedTo(user, pageable).map(this::mapToResponse);
    }

    @Override
    public void deleteTicket(Long id, String username) {
        Ticket ticket = getTicketEntityById(id);
        User user = getUserByUsername(username);
        if (!user.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("Only admins can delete tickets");
        }
        ticketRepository.delete(ticket);
        log.info("Ticket {} deleted by {}", ticket.getTicketNumber(), username);
    }

    @Override
    public TicketResponse assignTicket(Long ticketId, Long userId, String username) {
        Ticket ticket = getTicketEntityById(ticketId);
        User assignedTo = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        User updater = getUserByUsername(username);

        String oldAssignee = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : "Unassigned";
        ticket.setAssignedTo(assignedTo);

        logHistory(ticket, updater, "assignedTo", oldAssignee, assignedTo.getFullName(), "Ticket assigned");
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse updateTicketStatus(Long ticketId, String status, String resolutionDetails, String username) {
        Ticket ticket = getTicketEntityById(ticketId);
        User updater = getUserByUsername(username);
        TicketStatus newStatus = TicketStatus.valueOf(status.toUpperCase());

        if ((newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED)
                && (resolutionDetails == null || resolutionDetails.isBlank())) {
            throw new BadRequestException("Resolution details required for RESOLVED/CLOSED status");
        }

        String oldStatus = ticket.getCurrentStatus().name();
        ticket.setCurrentStatus(newStatus);
        if (ticket.getResponseDateTime() == null) {
            ticket.setResponseDateTime(LocalDateTime.now());
        }
        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            LocalDateTime now = LocalDateTime.now();
            ticket.setResolutionDateTime(now);
            ticket.setResolutionTimeMinutes(ChronoUnit.MINUTES.between(ticket.getGenerationDateTime(), now));
            ticket.setResolutionDetails(resolutionDetails);
        }

        logHistory(ticket, updater, "currentStatus", oldStatus, status, "Status updated");
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse addComment(Long ticketId, String content, boolean isInternal, String username) {
        Ticket ticket = getTicketEntityById(ticketId);
        User author = getUserByUsername(username);

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .author(author)
                .content(content)
                .isInternal(isInternal)
                .build();

        commentRepository.save(comment);
        return mapToResponse(ticketRepository.findById(ticketId).orElseThrow());
    }

    @Override
    public TicketResponse uploadAttachment(Long ticketId, MultipartFile file, String username) {
        Ticket ticket = getTicketEntityById(ticketId);
        User uploader = getUserByUsername(username);

        try {
            Path uploadPath = Paths.get(uploadDir, "tickets", String.valueOf(ticketId));
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Attachment attachment = Attachment.builder()
                    .ticket(ticket)
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .uploadedBy(uploader)
                    .build();

            attachmentRepository.save(attachment);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }

        return mapToResponse(ticketRepository.findById(ticketId).orElseThrow());
    }

    @Override
    public ByteArrayOutputStream exportToExcel(String status, String priority) {
        List<Ticket> tickets = getFilteredTickets(status, priority);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tickets");
            String[] headers = {"Ticket #", "Project", "Issue", "Assigned To", "Priority", "Status", "Support Level", "Created", "Resolution Time"};

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            int rowNum = 1;
            for (Ticket t : tickets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(t.getTicketNumber());
                row.createCell(1).setCellValue(t.getProject().getName());
                row.createCell(2).setCellValue(t.getIssueDescription().substring(0, Math.min(100, t.getIssueDescription().length())));
                row.createCell(3).setCellValue(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned");
                row.createCell(4).setCellValue(t.getPriority().name());
                row.createCell(5).setCellValue(t.getCurrentStatus().name());
                row.createCell(6).setCellValue(t.getSupportLevel().name());
                row.createCell(7).setCellValue(t.getGenerationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                row.createCell(8).setCellValue(t.getResolutionTimeMinutes() != null ? formatResolutionTime(t.getResolutionTimeMinutes()) : "N/A");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos;
        } catch (IOException e) {
            throw new BadRequestException("Failed to generate Excel: " + e.getMessage());
        }
    }

    @Override
    public ByteArrayOutputStream exportToPdf(String status, String priority) {
        // Simplified PDF export - returns basic text representation
        List<Ticket> tickets = getFilteredTickets(status, priority);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // In production, integrate iText for rich PDF output
        return baos;
    }

    // ─── Private Helpers ────────────────────────────────────────────────────

    private synchronized String generateTicketNumber() {
        Integer maxSeq = ticketRepository.getMaxTicketSequence();
        int next = (maxSeq != null ? maxSeq : 9000) + 1;
        return "INC-" + next;
    }

    private LocalDateTime calculateSlaDueDate(Priority priority, LocalDateTime from) {
        return switch (priority) {
            case P1_CRITICAL -> from.plusHours(4);
            case P2_HIGH -> from.plusHours(8);
            case P3_MEDIUM -> from.plusHours(24);
            case P4_LOW -> from.plusHours(72);
        };
    }

    private void logHistory(Ticket ticket, User changedBy, String fieldName,
                            String oldValue, String newValue, String description) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .changedBy(changedBy)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .build();
        historyRepository.save(history);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Ticket getTicketEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
    }

    private List<Ticket> getFilteredTickets(String status, String priority) {
        Specification<Ticket> spec = buildSpecification(null, null, null, priority, status, null, null, null, null);
        return ticketRepository.findAll(spec);
    }

    private Specification<Ticket> buildSpecification(
            String ticketNumber, Long projectId, Long assignedToId,
            String priority, String status, String supportLevel,
            LocalDate startDate, LocalDate endDate, String search) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (ticketNumber != null && !ticketNumber.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("ticketNumber")), "%" + ticketNumber.toLowerCase() + "%"));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (assignedToId != null) {
                predicates.add(cb.equal(root.get("assignedTo").get("id"), assignedToId));
            }
            if (priority != null && !priority.isBlank()) {
                predicates.add(cb.equal(root.get("priority"), Priority.valueOf(priority)));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("currentStatus"), TicketStatus.valueOf(status)));
            }
            if (supportLevel != null && !supportLevel.isBlank()) {
                predicates.add(cb.equal(root.get("supportLevel"), SupportLevel.valueOf(supportLevel)));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate.atTime(23, 59, 59)));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("ticketNumber")), like),
                        cb.like(cb.lower(root.get("issueDescription")), like)
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private String formatResolutionTime(Long minutes) {
        if (minutes == null) return "N/A";
        long days = minutes / (60 * 24);
        long hours = (minutes % (60 * 24)) / 60;
        long mins = minutes % 60;
        if (days > 0) return days + "d " + hours + "h " + mins + "m";
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        List<CommentResponse> comments = ticket.getComments().stream().map(c ->
                CommentResponse.builder()
                        .id(c.getId()).ticketId(ticket.getId())
                        .authorId(c.getAuthor().getId()).authorName(c.getAuthor().getFullName())
                        .authorRole(c.getAuthor().getRole().name())
                        .content(c.getContent()).isInternal(c.getIsInternal())
                        .createdAt(c.getCreatedAt()).build()
        ).collect(Collectors.toList());

        List<HistoryResponse> history = ticket.getHistory().stream().map(h ->
                HistoryResponse.builder()
                        .id(h.getId()).fieldName(h.getFieldName())
                        .oldValue(h.getOldValue()).newValue(h.getNewValue())
                        .description(h.getDescription())
                        .changedById(h.getChangedBy().getId())
                        .changedByName(h.getChangedBy().getFullName())
                        .changedAt(h.getChangedAt()).build()
        ).collect(Collectors.toList());

        List<AttachmentResponse> attachments = ticket.getAttachments().stream().map(a ->
                AttachmentResponse.builder()
                        .id(a.getId()).fileName(a.getFileName())
                        .fileSize(a.getFileSize()).contentType(a.getContentType())
                        .downloadUrl("/api/attachments/" + a.getId())
                        .uploadedById(a.getUploadedBy() != null ? a.getUploadedBy().getId() : null)
                        .uploadedByName(a.getUploadedBy() != null ? a.getUploadedBy().getFullName() : null)
                        .uploadedAt(a.getUploadedAt()).build()
        ).collect(Collectors.toList());

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .issueDescription(ticket.getIssueDescription())
                .projectId(ticket.getProject().getId())
                .projectName(ticket.getProject().getName())
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : null)
                .createdById(ticket.getCreatedBy().getId())
                .createdByName(ticket.getCreatedBy().getFullName())
                .supportLevel(ticket.getSupportLevel())
                .priority(ticket.getPriority())
                .generationDateTime(ticket.getGenerationDateTime())
                .responseDateTime(ticket.getResponseDateTime())
                .resolutionDateTime(ticket.getResolutionDateTime())
                .resolutionTimeMinutes(ticket.getResolutionTimeMinutes())
                .resolutionTimeFormatted(formatResolutionTime(ticket.getResolutionTimeMinutes()))
                .currentStatus(ticket.getCurrentStatus())
                .resolutionDetails(ticket.getResolutionDetails())
                .remarks(ticket.getRemarks())
                .slaBreached(ticket.getSlaBreached())
                .slaDueDateTime(ticket.getSlaDueDateTime())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .comments(comments)
                .history(history)
                .attachments(attachments)
                .build();
    }
}
