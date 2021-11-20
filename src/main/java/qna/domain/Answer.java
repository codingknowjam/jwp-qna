package qna.domain;

import qna.CannotDeleteException;
import qna.NotFoundException;
import qna.UnAuthorizedException;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 *     create table answer
 *         (
 *             id          bigint generated by default as identity,
 *             contents    clob,
 *             created_at  timestamp not null,
 *             deleted     boolean   not null,
 *             question_id bigint,
 *             updated_at  timestamp,
 *             writer_id   bigint,
 *             primary key (id)
 *           )
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Answer extends BaseEntity {

	@Lob
	private String contents;

	@Column(nullable = false)
	private boolean deleted;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id")
	private Question question;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "writer_id")
	private User writer;

	protected Answer() {
	}

	public Answer(User writer, Question question, String contents) {
		this(null, writer, question, contents);
	}

	public Answer(Long id, User writer, Question question, String contents) {
		this.id = id;

		if (Objects.isNull(writer)) {
			throw new UnAuthorizedException();
		}

		if (Objects.isNull(question)) {
			throw new NotFoundException();
		}

		setWriter(writer);
		this.contents = contents;
		this.question = question;
	}

	private void setWriter(User writer) {
		this.writer = writer;
		writer.addAnswer(this);
	}

	public boolean isOwner(User writer) {
		return this.writer.equals(writer);
	}

	public void toQuestion(Question question) {
		this.question = question;
	}

	public DeleteHistory delete(User loginUser) throws CannotDeleteException {
		validateAnswer(loginUser);
		this.deleted = true;
		return new DeleteHistory(ContentType.ANSWER, id, writer);
	}

	private void validateAnswer(User loginUser) throws CannotDeleteException {
		if (!this.isOwner(loginUser) && !this.isDeleted()) {
			throw new CannotDeleteException("다른 사람이 쓴 답변이 있어 삭제할 수 없습니다.");
		}
	}

	public boolean isDeleted() {
		return deleted;
	}

	public Long getId() {
		return id;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public User getWriter() {
		return writer;
	}

	@Override
	public String toString() {
		return "Answer{" +
			"id=" + id +
			", writerId=" + writer.getId() +
			", questionId=" + question.getId() +
			", contents='" + contents + '\'' +
			", deleted=" + deleted +
			'}';
	}

}
