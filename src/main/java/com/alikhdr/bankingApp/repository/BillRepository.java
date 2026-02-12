package com.alikhdr.bankingApp.repository;

import com.alikhdr.bankingApp.entity.Bill;
import com.alikhdr.bankingApp.entity.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long>
{
    List<Bill> findAllByDueDateBeforeAndStatus(LocalDateTime dateTime, BillStatus status);
}
