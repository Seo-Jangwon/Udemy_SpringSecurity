package com.eazybytes.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eazybytes.model.Notice;
import com.eazybytes.repository.NoticeRepository;

@RestController
public class NoticesController {

    @Autowired
    private NoticeRepository noticeRepository;

    @GetMapping("/notices")
    public ResponseEntity<List<Notice>> getNotices() {
        List<Notice> notices = noticeRepository.findAllActiveNotices();
        
        // 공지사항 목록이 null이 아닌 경우, 응답 본문에 공지사항 목록을 포함하여 응답을 반환.
        if (notices != null ) {
            // ResponseEntity.ok()는 HTTP 상태 코드 200 OK를 설정.
            return ResponseEntity.ok()
                // cacheControl() 메서드를 사용하여 응답에 Cache-Control 헤더를 추가.
                // CacheControl.maxAge(60, TimeUnit.SECONDS)는 캐시 유효 기간을 설정.
                // maxAge(60, TimeUnit.SECONDS)는 클라이언트와 중간 캐시가 이 응답을 60초 동안 재사용할 수 있음을 의미.
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))
                // body() 메서드를 사용하여 응답 본문에 공지사항 목록을 포함.
                .body(notices);
        } else {
            // 공지사항 목록이 null인 경우, null을 반환. 이 부분은 개선이 필요할 수 있음.
            return null;
        }
    }

}
