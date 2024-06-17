package io.hhplus.tdd.point;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository {
    List<PointHistory> selectAllByUserId(long id);

    PointHistory insert(long userId, long amount, TransactionType type, long updateMillis);
}
