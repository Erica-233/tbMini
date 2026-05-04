package com.example.tbmini.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.tbmini.domain.entity.Comment;
import com.example.tbmini.domain.entity.ModerationAction;
import com.example.tbmini.domain.entity.Post;
import com.example.tbmini.domain.entity.Report;
import com.example.tbmini.dto.ReportRequest;
import com.example.tbmini.dto.ReportVo;
import com.example.tbmini.mapper.CommentMapper;
import com.example.tbmini.mapper.ModerationActionMapper;
import com.example.tbmini.mapper.PostMapper;
import com.example.tbmini.mapper.ReportMapper;
import com.example.tbmini.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ModerationActionMapper moderationActionMapper;
    private final UserMapper userMapper;

    @Transactional
    public void report(ReportRequest req, Long reporterId) {
        QueryWrapper<Report> qw = new QueryWrapper<>();
        qw.eq("reporter_id", reporterId)
          .eq("target_type", req.getTargetType())
          .eq("target_id", req.getTargetId())
          .eq("is_open", 1);
        Report exist = reportMapper.selectOne(qw);
        if (exist != null) {
            return;
        }
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(req.getTargetType());
        report.setTargetId(req.getTargetId());
        report.setReasonCode(req.getReasonCode());
        report.setReasonText(req.getReasonText());
        report.setStatus("OPEN");
        report.setIsOpen(true);
        reportMapper.insert(report);

        int openCount = reportMapper.countOpenByTarget(req.getTargetType(), req.getTargetId());
        if (openCount >= 3) {
            if ("POST".equals(req.getTargetType())) {
                Post post = postMapper.selectById(req.getTargetId());
                if (post != null && !"PENDING_REVIEW".equals(post.getStatus()) && !"REMOVED".equals(post.getStatus())) {
                    post.setStatus("PENDING_REVIEW");
                    postMapper.updateById(post);
                }
            } else if ("COMMENT".equals(req.getTargetType())) {
                Comment comment = commentMapper.selectById(req.getTargetId());
                if (comment != null && !"PENDING_REVIEW".equals(comment.getStatus()) && !"REMOVED".equals(comment.getStatus())) {
                    comment.setStatus("PENDING_REVIEW");
                    commentMapper.updateById(comment);
                }
            }
            ModerationAction action = new ModerationAction();
            action.setModeratorId(null);
            action.setTargetType(req.getTargetType());
            action.setTargetId(req.getTargetId());
            action.setAction("AUTO_FLAGGED");
            action.setReason("Open reports >= 3");
            moderationActionMapper.insert(action);
        }
    }

    public List<ReportVo> listOpenReports() {
        QueryWrapper<Report> qw = new QueryWrapper<>();
        qw.eq("status", "OPEN").orderByDesc("created_at");
        return reportMapper.selectList(qw).stream().map(this::toVo).collect(Collectors.toList());
    }

    private ReportVo toVo(Report r) {
        ReportVo vo = new ReportVo();
        vo.setId(r.getId());
        vo.setReporterId(r.getReporterId());
        vo.setTargetType(r.getTargetType());
        vo.setTargetId(r.getTargetId());
        vo.setReasonCode(r.getReasonCode());
        vo.setReasonText(r.getReasonText());
        vo.setStatus(r.getStatus());
        vo.setCreatedAt(r.getCreatedAt());
        vo.setResolvedAt(r.getResolvedAt());
        var user = userMapper.selectById(r.getReporterId());
        if (user != null) vo.setReporterNickname(user.getNickname());
        return vo;
    }
}
