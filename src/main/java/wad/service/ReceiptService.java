/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wad.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wad.domain.Receipt;
import wad.repository.ReceiptRepository;

/**
 *
 * @author teemu
 */
@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

    public boolean checkReceiptName(String name) {
        long count = receiptRepository.count();
        List<Receipt> list;
        
        if (count > 0) {
            list = receiptRepository.findAll();
            for (Receipt r : list) {
                if (r.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
}
