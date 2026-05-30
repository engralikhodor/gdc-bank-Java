package com.alikhdr.bankingApp.repository;

import com.alikhdr.bankingApp.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepository extends JpaRepository<Option, Long>
{
}
