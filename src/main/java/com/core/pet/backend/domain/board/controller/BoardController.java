package com.core.pet.backend.domain.board.controller;

import com.core.pet.backend.domain.board.dto.BoardRequestDto;
import com.core.pet.backend.domain.board.dto.BoardResponseDto;
import com.core.pet.backend.domain.board.service.BoardService;
import com.core.pet.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Board", description = "게시판 API")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "게시글 목록 조회 (페이징)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardResponseDto.Summary>>> getBoards(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoards(pageable)));
    }

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponseDto.Detail>> getBoard(@PathVariable Long boardId) {
        return ResponseEntity.ok(ApiResponse.ok(boardService.getBoard(boardId)));
    }

    @Operation(summary = "게시글 작성")
    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponseDto.Detail>> createBoard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid BoardRequestDto.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("게시글이 작성되었습니다.",
                        boardService.createBoard(userDetails.getUsername(), request)));
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponseDto.Detail>> updateBoard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long boardId,
            @RequestBody @Valid BoardRequestDto.Update request) {
        return ResponseEntity.ok(ApiResponse.ok("게시글이 수정되었습니다.",
                boardService.updateBoard(userDetails.getUsername(), boardId, request)));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long boardId) {
        boardService.deleteBoard(userDetails.getUsername(), boardId);
        return ResponseEntity.ok(ApiResponse.ok("게시글이 삭제되었습니다.", null));
    }
}
