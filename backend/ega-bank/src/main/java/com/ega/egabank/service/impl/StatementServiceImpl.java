package com.ega.egabank.service.impl;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ega.egabank.entity.Account;
import com.ega.egabank.entity.Transaction;
import com.ega.egabank.exception.ResourceNotFoundException;
import com.ega.egabank.exception.StatementGenerationException;
import com.ega.egabank.repository.AccountRepository;
import com.ega.egabank.repository.TransactionRepository;
import com.ega.egabank.service.StatementService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implémentation du service de génération de relevés de compte
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatementServiceImpl implements StatementService {

        private final AccountRepository accountRepository;
        private final TransactionRepository transactionRepository;

        private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(0, 82, 147);
        private static final DeviceRgb HEADER_BG_COLOR = new DeviceRgb(240, 240, 240);
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        @Override
        public byte[] generateStatement(String numeroCompte, LocalDate debut, LocalDate fin) {
                log.info("Génération du relevé pour {} du {} au {}", numeroCompte, debut, fin);

                Account account = accountRepository.findByNumeroCompteWithClient(numeroCompte)
                                .orElseThrow(() -> new ResourceNotFoundException("Compte", "numéro", numeroCompte));

                LocalDateTime debutDateTime = debut.atStartOfDay();
                LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);

                List<Transaction> transactions = transactionRepository.findByCompteAndPeriod(
                                numeroCompte, debutDateTime, finDateTime);

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdf = new PdfDocument(writer);
                        Document document = new Document(pdf, PageSize.A4);
                        document.setMargins(40, 40, 40, 40);

                        // En-tête
                        addHeader(document);

                        // Informations du compte
                        addAccountInfo(document, account, debut, fin);

                        // Tableau des transactions
                        addTransactionsTable(document, transactions);

                        // Résumé
                        addSummary(document, account, transactions);

                        // Pied de page
                        addFooter(document);

                        document.close();

                        log.info("Relevé généré avec succès - {} transactions", transactions.size());
                        return baos.toByteArray();

                } catch (Exception e) {
                        log.error("Erreur lors de la génération du relevé", e);
                        throw new StatementGenerationException("Erreur lors de la génération du relevé de compte", e);
                }
        }

        private void addHeader(Document document) throws Exception {
                Paragraph title = new Paragraph("BANQUE EGA")
                                .setFont(PdfFontFactory.createFont())
                                .setFontSize(24)
                                .setFontColor(PRIMARY_COLOR)
                                .setBold()
                                .setTextAlignment(TextAlignment.CENTER);
                document.add(title);

                Paragraph subtitle = new Paragraph("Relevé de Compte")
                                .setFontSize(16)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(20);
                document.add(subtitle);
        }

        private void addAccountInfo(Document document, Account account, LocalDate debut, LocalDate fin) {
                Table infoTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginBottom(20);

                // Colonne gauche - Infos client
                Cell leftCell = new Cell()
                                .setBorder(Border.NO_BORDER)
                                .add(new Paragraph("Titulaire: " + account.getProprietaire().getNomComplet()).setBold())
                                .add(new Paragraph("Numéro de compte: " + account.getNumeroCompte()))
                                .add(new Paragraph("Type de compte: " + account.getTypeCompte().getLibelle()));
                infoTable.addCell(leftCell);

                // Colonne droite - Période
                Cell rightCell = new Cell()
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .add(new Paragraph("Période du relevé").setBold())
                                .add(new Paragraph("Du " + debut.format(DATE_FORMATTER)))
                                .add(new Paragraph("Au " + fin.format(DATE_FORMATTER)));
                infoTable.addCell(rightCell);

                document.add(infoTable);
        }

        private void addTransactionsTable(Document document, List<Transaction> transactions) {
                document.add(new Paragraph("Détail des opérations")
                                .setBold()
                                .setFontSize(12)
                                .setMarginTop(10)
                                .setMarginBottom(10));

                Table table = new Table(UnitValue.createPercentArray(new float[] { 15, 25, 20, 20, 20 }))
                                .setWidth(UnitValue.createPercentValue(100));

                // En-têtes
                addTableHeader(table, "Date");
                addTableHeader(table, "Opération");
                addTableHeader(table, "Montant");
                addTableHeader(table, "Solde avant");
                addTableHeader(table, "Solde après");

                // Lignes
                if (transactions.isEmpty()) {
                        Cell emptyCell = new Cell(1, 5)
                                        .add(new Paragraph("Aucune transaction sur cette période"))
                                        .setTextAlignment(TextAlignment.CENTER)
                                        .setPadding(20);
                        table.addCell(emptyCell);
                } else {
                        for (Transaction t : transactions) {
                                table.addCell(createCell(t.getDateTransaction().format(DATETIME_FORMATTER)));
                                table.addCell(createCell(t.getType().getLibelle()));
                                table.addCell(createAmountCell(t.getMontant().toString(), t.getType().name()));
                                table.addCell(createCell(
                                                t.getSoldeAvant() != null ? t.getSoldeAvant().toString() : "-"));
                                table.addCell(createCell(
                                                t.getSoldeApres() != null ? t.getSoldeApres().toString() : "-"));
                        }
                }

                document.add(table);
        }

        private void addTableHeader(Table table, String text) {
                Cell cell = new Cell()
                                .add(new Paragraph(text).setBold())
                                .setBackgroundColor(HEADER_BG_COLOR)
                                .setPadding(8)
                                .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
        }

        private Cell createCell(String text) {
                return new Cell()
                                .add(new Paragraph(text))
                                .setPadding(5)
                                .setTextAlignment(TextAlignment.CENTER);
        }

        private Cell createAmountCell(String amount, String type) {
                Cell cell = new Cell()
                                .add(new Paragraph(amount + " XOF"))
                                .setPadding(5)
                                .setTextAlignment(TextAlignment.RIGHT);

                if (type.contains("RETRAIT") || type.contains("SORTANT")) {
                        cell.setFontColor(new DeviceRgb(220, 53, 69)); // Rouge
                } else {
                        cell.setFontColor(new DeviceRgb(40, 167, 69)); // Vert
                }

                return cell;
        }

        private void addSummary(Document document, Account account, List<Transaction> transactions) {
                document.add(new Paragraph("")
                                .setMarginTop(20));

                Table summaryTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                                .setWidth(UnitValue.createPercentValue(50))
                                .setMarginTop(20);

                summaryTable
                                .addCell(new Cell().add(new Paragraph("Nombre d'opérations:").setBold())
                                                .setBorder(Border.NO_BORDER));
                summaryTable.addCell(new Cell().add(new Paragraph(String.valueOf(transactions.size())))
                                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

                summaryTable.addCell(
                                new Cell().add(new Paragraph("Solde actuel:").setBold()).setBorder(Border.NO_BORDER));
                summaryTable.addCell(new Cell().add(new Paragraph(account.getSolde() + " XOF").setBold())
                                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

                document.add(summaryTable);
        }

        private void addFooter(Document document) {
                document.add(new Paragraph("")
                                .setMarginTop(40));

                Paragraph footer = new Paragraph("Document généré le " + LocalDateTime.now().format(DATETIME_FORMATTER))
                                .setFontSize(10)
                                .setFontColor(ColorConstants.GRAY)
                                .setTextAlignment(TextAlignment.CENTER);
                document.add(footer);

                Paragraph disclaimer = new Paragraph("Ce document est un relevé informatif. " +
                                "Pour toute réclamation, veuillez contacter votre agence.")
                                .setFontSize(8)
                                .setFontColor(ColorConstants.GRAY)
                                .setTextAlignment(TextAlignment.CENTER);
                document.add(disclaimer);
        }
}
