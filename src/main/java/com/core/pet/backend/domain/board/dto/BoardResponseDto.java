package com.core.pet.backend.domain.board.dto;

import com.core.pet.backend.domain.board.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class BoardResponseDto {

    @Getter
    @Builder
    public static class Summary {
        private Long id;
        private String title;
        private String authorName;
        private LocalDateTime createdAt;

        public static Summary from(Board board) {
            return Summary.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .authorName(board.getMember().getName())
                    .createdAt(board.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private String title;
        private String content;
        private String authorName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Detail from(Board board) {
            return Detail.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .authorName(board.getMember().getName())
                    .createdAt(board.getCreatedAt())
                    .updatedAt(board.getUpdatedAt())
                    .build();
        }
    }
}
