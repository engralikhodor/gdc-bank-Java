package com.alikhdr.bankingApp.controller;

import com.alikhdr.bankingApp.entity.Bill;
import com.alikhdr.bankingApp.entity.BillStatus;
import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.repository.BillRepository;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import com.alikhdr.bankingApp.service.BillSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bills")
public class BillController
{
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final BillSchedulerService billSchedulerService;

    // for testing only
    @PostMapping("/create")
    public ResponseEntity<String> createTestBill(@RequestParam String email,
                                                 @RequestParam BigDecimal amount)
    {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(RuntimeException::new);

        Bill bill = Bill.builder()
                .billerName("E.D.L")
                .amount(amount)
                .dueDate(LocalDateTime.now().minusHours(1))
                .status(BillStatus.PENDING)
                .customer(customer)
                .build();

        billRepository.save(bill);
        return ResponseEntity.ok("Test bill created.");
    }

    // for testing only
    @PostMapping("/trigger-payments")
    public ResponseEntity<String> triggerPayments()
    {
        billSchedulerService.processPendingBills();
        return ResponseEntity.ok("Payment process triggered. Check your S3 bucket and logs!");
    }
}
