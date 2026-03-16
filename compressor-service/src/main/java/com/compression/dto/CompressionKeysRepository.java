package com.compression.dto;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.compression.model.CompressionKeysEntity;

@Repository
public interface CompressionKeysRepository extends CrudRepository<CompressionKeysEntity, Integer> {

}
