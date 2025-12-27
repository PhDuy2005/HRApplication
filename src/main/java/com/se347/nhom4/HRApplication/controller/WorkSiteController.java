package com.se347.nhom4.HRApplication.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqWorkSite;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResWorkSite;
import com.se347.nhom4.HRApplication.service.WorkSiteService;
import com.se347.nhom4.HRApplication.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/work-sites")
@RequiredArgsConstructor
public class WorkSiteController {

    private final WorkSiteService workSiteService;

    // Tạo WorkSite
    @PostMapping
    @ApiMessage("Tạo địa điểm làm việc mới")
    public ResWorkSite create(@RequestBody ReqWorkSite req) {
        return workSiteService.create(req);
    }

    // Lấy danh sách WorkSite đang active
    @GetMapping("/active")
    @ApiMessage("Lấy danh sách địa điểm làm việc đang hoạt động")
    public List<ResWorkSite> getActive() {
        return workSiteService.getActive();
    }

    // Lấy chi tiết theo id
    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết địa điểm làm việc theo ID")
    public ResWorkSite getById(@PathVariable Long id) {
        return workSiteService.getById(id);
    }
}
