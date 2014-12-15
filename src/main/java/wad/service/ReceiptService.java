/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wad.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wad.repository.ReceiptRepository;

/**
 *
 * @author teemu
 */
@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

}
