package com.alikhdr.bankingApp.service;

import com.alikhdr.bankingApp.entity.Bill;
import com.alikhdr.bankingApp.entity.BillStatus;
import com.alikhdr.bankingApp.entity.Customer;
import com.alikhdr.bankingApp.repository.BillRepository;
import com.alikhdr.bankingApp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillSchedulerService
{
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final S3Service s3Service;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processPendingBills()
    {
        log.info("Starting scheduled bill payment process...");

        // bills due today and PENDING
        List<Bill> dueBills = billRepository.findAllByDueDateBeforeAndStatus(
                LocalDateTime.now(), BillStatus.PENDING);

        for (Bill bill : dueBills)
        {
            Customer customer = bill.getCustomer();

            // check customer balance first
            if (customer.getAccountBalance().compareTo(bill.getAmount()) >= 0)
            {

                // deduct balance
                customer.setAccountBalance(customer.getAccountBalance().subtract(bill.getAmount()));
                bill.setStatus(BillStatus.PAID);
                customerRepository.save(customer);

                // generate Receipt Text
                String receiptData = String.format(
                        "BANK RECEIPT\nBiller: %s\nAmount: %s\nDate: %s\nAccount: %s",
                        bill.getBillerName(), bill.getAmount(), LocalDateTime.now(), customer.getAccountNumber()
                );

                // upload TO AWS S3
                try
                {
                    String s3Url = s3Service.uploadBillReceipt(bill.getId(), receiptData);
                    bill.setReceiptUrl(s3Url);
                    log.info("Receipt uploaded to S3: {}", s3Url);
                }
                catch (Exception e)
                {
                    log.error("Failed to upload receipt for bill {}: {}", bill.getId(), e.getMessage());
                }

                billRepository.save(bill);
            }
            else
            {
                bill.setStatus(BillStatus.FAILED);
                billRepository.save(bill);
                log.warn("Bill {} failed due to insufficient funds for customer {}", bill.getId(), customer.getAccountNumber());
            }
        }
    }
}
// gdc-bank-receipts-s3
// gdc-bank-service-IAM
// Permissions summary: AmazonS3FullAccess
// create Access keys
