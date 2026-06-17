package com.unihub.shared.api.external;

import com.unihub.shared.api.dto.external.StudentPublicInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface StudentPublicApi {

    Page<StudentPublicInfo> getStudentsByUniversityId(UUID universityId, Pageable pageable);

}