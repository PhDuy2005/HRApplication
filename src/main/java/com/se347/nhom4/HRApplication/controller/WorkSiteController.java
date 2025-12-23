package com.se347.nhom4.HRApplication.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqWorkSite;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResWorkSite;
import com.se347.nhom4.HRApplication.service.WorkSiteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/work-sites")
@RequiredArgsConstructor
public class WorkSiteController {

    private final WorkSiteService workSiteService;

    // Tạo WorkSite
    @PostMapping
    public ResWorkSite create(@RequestBody ReqWorkSite req) {
        return workSiteService.create(req);
    }

    // Lấy danh sách WorkSite đang active
    @GetMapping("/active")
    public List<ResWorkSite> getActive() {
        return workSiteService.getActive();
    }

    // Lấy chi tiết theo id
    @GetMapping("/{id}")
    public ResWorkSite getById(@PathVariable Long id) {
        return workSiteService.getById(id);
    }
}
