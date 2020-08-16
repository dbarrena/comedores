package com.dbxprts.comedores.api;

import com.dbxprts.comedores.model.ApiRequest;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.print.PrintException;
import java.awt.print.PrinterException;
import java.io.IOException;

@RestController
@RequestMapping("ticket")
@CrossOrigin(origins = "*")
public class TicketController {
    @Autowired
    TicketService ticketService;

    @PostMapping("generate")
    public ResponseEntity<?> generateTicket(@RequestBody ApiRequest apiRequest) throws DocumentException, PrintException, IOException, PrinterException {
        ticketService.generatePDF(apiRequest);

        return ResponseEntity
                .ok()
                .body("PDF generated");

    }
}
