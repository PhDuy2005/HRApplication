package com.se347.nhom4.HRApplication.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.se347.nhom4.HRApplication.domain.requestDTO.ReqWorkSite;
import com.se347.nhom4.HRApplication.domain.responseDTO.ResWorkSite;
import com.se347.nhom4.HRApplication.domain.table.WorkSite;
import com.se347.nhom4.HRApplication.repository.WorkSiteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkSiteService {

    private final WorkSiteRepository workSiteRepository;

    @Transactional
    public ResWorkSite create(ReqWorkSite req) {
        // validate tối thiểu
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("WorkSite name is required");
        }
        if (req.getLatitude() == null || req.getLongitude() == null) {
            throw new IllegalArgumentException("Latitude/Longitude is required");
        }
        if (req.getRadiusMeters() == null || req.getRadiusMeters() <= 0) {
            throw new IllegalArgumentException("RadiusMeters must be > 0");
        }

        Instant now = Instant.now();

        WorkSite site = new WorkSite();
        site.setName(req.getName());
        site.setAddress(req.getAddress());

        site.setLatitude(req.getLatitude());
        site.setLongitude(req.getLongitude());

        site.setRadiusMeters(req.getRadiusMeters());
        site.setAllowedAccuracyMaxMeters(req.getAllowedAccuracyMaxMeters());

        site.setActive(true);
        site.setCreatedAt(now);
        site.setUpdatedAt(now);

        WorkSite saved = workSiteRepository.save(site);
        return toRes(saved);
    }

    @Transactional(readOnly = true)
    public List<ResWorkSite> getActive() {
        return workSiteRepository.findByActiveTrue()
                .stream()
                .map(this::toRes)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResWorkSite getById(Long id) {
        WorkSite site = workSiteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("WorkSite not found with id: " + id));
        return toRes(site);
    }

    private ResWorkSite toRes(WorkSite s) {
        return ResWorkSite.builder()
                .id(s.getId())
                .name(s.getName())
                .address(s.getAddress())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .radiusMeters(s.getRadiusMeters())
                .allowedAccuracyMaxMeters(s.getAllowedAccuracyMaxMeters())
                .active(s.getActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
