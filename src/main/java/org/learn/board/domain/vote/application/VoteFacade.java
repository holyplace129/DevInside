package org.learn.board.domain.vote.application;

import lombok.RequiredArgsConstructor;
import org.learn.board.domain.comment.domain.Comment;
import org.learn.board.domain.comment.domain.repository.CommentRepository;
import org.learn.board.domain.post.domain.Post;
import org.learn.board.domain.post.domain.repository.PostRepository;
import org.learn.board.domain.vote.domain.CommentVote;
import org.learn.board.domain.vote.domain.PostVote;
import org.learn.board.domain.vote.domain.repository.CommentVoteRepository;
import org.learn.board.domain.vote.domain.repository.PostVoteRepository;
import org.learn.board.global.aop.DistributedLock;
import org.learn.board.global.error.ErrorCode;
import org.learn.board.global.error.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteFacade {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostVoteRepository postVoteRepository;
    private final CommentVoteRepository commentVoteRepository;


    // 게시글 추천
    @DistributedLock(key = "'post:' + #postId")
    public void likePost(Long postId, String voterIp) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POST_NOT_FOUND));

        postVoteRepository.findByPostAndVoterIp(post, voterIp).ifPresent(v -> {
            throw new EntityNotFoundException(ErrorCode.ALREADY_VOTED);
        });

        PostVote postVote = PostVote.builder()
                .post(post)
                .voterIp(voterIp)
                .voteType(PostVote.VoteType.LIKE)
                .build();
        postVoteRepository.save(postVote);

        post.increaseLikeCount();
    }

    // 게시글 비추천
    @DistributedLock(key = "'post:' + #postId")
    public void dislikePost(Long postId, String voterIp) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.POST_NOT_FOUND));

        postVoteRepository.findByPostAndVoterIp(post, voterIp).ifPresent(v -> {
            throw new EntityNotFoundException(ErrorCode.ALREADY_VOTED);
        });

        PostVote postVote = PostVote.builder()
                .post(post)
                .voterIp(voterIp)
                .voteType(PostVote.VoteType.DISLIKE)
                .build();
        postVoteRepository.save(postVote);

        post.increaseDislikeCount();
    }

    // 댓글 추천
    @DistributedLock(key = "'comment:' + #commentId")
    public void likeComment(Long commentId, String voterIp) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        commentVoteRepository.findByCommentAndVoterIp(comment, voterIp).ifPresent(v -> {
            throw new EntityNotFoundException(ErrorCode.ALREADY_VOTED);
        });

        CommentVote commentVote = CommentVote.builder()
                .comment(comment)
                .voterIp(voterIp)
                .build();
        commentVoteRepository.save(commentVote);

        comment.increaseLikeCount();
    }
}
