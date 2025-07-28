package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.response.MyGalleryResponse;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.repository.NcutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {

    private final NcutRepository ncutRepository;

    @Override
    public MyGalleryResponse getMyGallery(String userUuid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Ncut> ncutPage = ncutRepository.findByUserUuidOrderByCreatedAtDesc(userUuid, pageable);
        return MyGalleryResponse.fromPage(ncutPage);
    }
}