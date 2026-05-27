package com.unihub.shared.application;

import com.unihub.shared.api.dto.CountryResponse;
import com.unihub.shared.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CountryUseCase {

    private final CountryRepository countryRepository;

    public List<CountryResponse> getAll() {
        return countryRepository.findAll()
                .stream()
                .map(c -> new CountryResponse(c.getId(), c.getName()))
                .toList();
    }
}
