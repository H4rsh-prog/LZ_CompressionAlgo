package com.compression.model;

import java.util.ArrayList;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressionKeysEntity {
	@Id
	private int dataHash;
	private ArrayList<ArrayList<String>> sortedHexKeys;
}
