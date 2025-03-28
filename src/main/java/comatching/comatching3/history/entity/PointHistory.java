package comatching.comatching3.history.entity;

import comatching.comatching3.history.enums.PointHistoryType;
import comatching.comatching3.pay.entity.Orders;
import comatching.comatching3.pay.entity.TossPayment;
import comatching.comatching3.users.entity.Users;
import comatching.comatching3.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "point_history")
public class PointHistory extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "point_history_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_id")
	private Users users;


	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "match_history_id")
	private MatchingHistory matchingHistory;


	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Orders orders;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "admin_id")
//	private Admin approver;

	@Enumerated(EnumType.STRING)
	private PointHistoryType pointHistoryType;

	// 증가/감소한 포인트 양
	private Long changeAmount;

	private String reason;

	// 결과 픽미 횟수
	private Integer pickMe;

	// 결과적으로 남은 포인트
	private Long totalPoint;

	@Builder
	public PointHistory(Orders orders, MatchingHistory matchingHistory, Users users, PointHistoryType pointHistoryType, Long changeAmount, String reason, Integer pickMe, Long totalPoint) {
		this.users = users;
		this.pointHistoryType = pointHistoryType;
		this.changeAmount = changeAmount;
		this.reason = reason;
		this.pickMe = pickMe;
		this.totalPoint = totalPoint;
		this.orders = orders;
		this.matchingHistory = matchingHistory;
	}

	public void setTotalPoint(Long totalPoint) {
		this.totalPoint = totalPoint;
	}

	public void setPickMe(Integer pickMe) {
		this.pickMe = pickMe;
	}

	public void setMatchingHistory(MatchingHistory matchingHistory) {
		this.matchingHistory = matchingHistory;
	}

	public void setOrders(Orders orders) {
		this.orders = orders;
	}
}
