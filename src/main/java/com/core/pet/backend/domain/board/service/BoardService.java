package com.core.pet.backend.domain.board.service;

import com.core.pet.backend.domain.board.entity.Board;
import com.core.pet.backend.domain.board.dto.BoardRequestDto;
import com.core.pet.backend.domain.board.dto.BoardResponseDto;
import com.core.pet.backend.domain.board.repository.BoardRepository;
import com.core.pet.backend.global.exception.CustomException;
import com.core.pet.backend.global.exception.ErrorCode;
import com.core.pet.backend.domain.member.entity.Member;
import com.core.pet.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public Page<BoardResponseDto.Summary> getBoards(Pageable pageable) {
        return boardRepository.findAllWithMember(pageable)
                .map(BoardResponseDto.Summary::from);
    }

    public BoardResponseDto.Detail getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        return BoardResponseDto.Detail.from(board);
    }

    @Transactional
    public BoardResponseDto.Detail createBoard(String email, BoardRequestDto.Create request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        Board board = Board.create(request.getTitle(), request.getContent(), member);
        return BoardResponseDto.Detail.from(boardRepository.save(board));
    }

    @Transactional
    public BoardResponseDto.Detail updateBoard(String email, Long boardId, BoardRequestDto.Update request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getMember().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.BOARD_FORBIDDEN);
        }

        board.update(request.getTitle(), request.getContent());
        return BoardResponseDto.Detail.from(board);
    }

    @Transactional
    public void deleteBoard(String email, Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        if (!board.getMember().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.BOARD_FORBIDDEN);
        }

        boardRepository.delete(board);
    }
}
