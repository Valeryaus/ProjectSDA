package converterTSEtoCSV;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Main {

    // public static final String INPUT_PATH = "C:/Temp/Input.csv"; // path for JAR file
    // public static final String OUTPUT_PATH = "C:/Temp/Output.csv"; // path for JAR file
    // public static final String ERROR_FILE_PATH = "C:/Temp/TSE_file_error_list.txt"; // path for JAR file

    public static final String INPUT_PATH = "src/main/java/converterTSEtoCSV/Input.csv"; // path for IDE console
    public static final String OUTPUT_PATH = "src/main/java/converterTSEtoCSV/Output.csv"; // path for IDE console
    public static final String ERROR_FILE_PATH = "src/main/java/converterTSEtoCSV/TSE_file_error_list.txt"; // path for IDE console

    public static void main(String[] args) throws IOException {
        // CSV failo nuskaitymo logika
        Session session = HibernateUtility.getSessionFactory().openSession();

        List<List<String>> records = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(INPUT_PATH))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(";"); // array be apribojimo nes []...
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // prekiu is ArrayList'o ikelimas i duomenu baze ir konvertavimo logika
        for (List<String> record : records) {
            Item item = Item.builder()
                    .partNumber(record.get(0))
                    .description(record.get(1))
                    .functionGroup(record.get(2))
                    .supplierProductGroup(record.get(3))
                    .unitOfMeasure(record.get(4))
                    .bulkQty(Integer.valueOf(record.get(5)))
                    .supplierNumber(record.get(6))
                    .marketingCode(record.get(7))
                    .fobNetPrice(new BigDecimal(record.get(8).replace(",", ".")))
                    .currencyCode(record.get(9))
                    .countryOfOrigin(record.get(10))
                    .blockingCode(record.get(11))
                    .weight(record.get(12).equals("") ? 0 : Integer.parseInt(record.get(12)))
                    .volume(record.get(13))
                    .tariffNo(record.get(14))
                    .companyCode(record.get(15))
                    .environmentalFee1(record.get(16))
                    .environmentalFee2(record.get(17))
                    .environmentalFee3(record.get(18))
                    .environmentalFee4(record.get(19))
                    .retailPrice(new BigDecimal(record.get(20).replace(",", ".")))
                    .dealerNetPrice(new BigDecimal(record.get(21).replace(",", ".")))
                    .passiveFlag(record.get(22))
                    .discountCode(Integer.valueOf(record.get(23)))
                    .build();

            session.beginTransaction();
            session.persist(item);
            session.getTransaction().commit();
        }

        //select'inam viska is MySQL duomenu bazes ir patalpinam i query.list'a
        Query query = session.createQuery("from Item");
        List<Item> items = query.list();

        System.out.println("bandomasis spausdinimas visu detaliu is duomenu bazes lenteles" + items);
        //cia jau ateina su nupjautais nuliais, jeigu pries konvertavima buvo be nuliu
        //bet duomenu bazeje visos kainos atrodo gerai ir vienodai (su vienodais rekvizitais)...?

        //klaidingu duomenu patikrinimo logika
        List<Item> zeroWeightItems = items.stream().filter(item -> item.getWeight() == 0).collect(Collectors.toList());

        //surasymas i klaidu faila
        for (Item zeroWeightItem : zeroWeightItems) {
            FileWriter errorFileWriter = new FileWriter(ERROR_FILE_PATH, true);
            PrintWriter errorPrintWriter = new PrintWriter(errorFileWriter, true);
            errorPrintWriter.println("\r\n" + "List of items with errors at " + LocalDateTime.now() + ": \r\n"
                    + zeroWeightItem.getPartNumber() + " / "
                    + zeroWeightItem.getDescription() + " -zero or missing weight of item- "
                    + zeroWeightItem.getWeight() + " gramme;");
            errorPrintWriter.close();
        }
        //teisingu duomenu isrinkimas (filtravimas) is duomenu bazes
        List<Item> filteredGodItems = items.stream().filter(item -> item.getWeight() > 0)
                .filter(item -> item.getFobNetPrice().compareTo(BigDecimal.ZERO) > 0)
                .filter(item -> item.getRetailPrice().compareTo(BigDecimal.ZERO) > 0)
                .filter(item -> item.getDealerNetPrice().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        System.out.println("bandomasis spausdinimas visu (is duomenu bazes lenteles) isfiltruotu Geru detaliu " + filteredGodItems);

        //atsikratom ID "stulpelio" ir paruosiam pagal reikiama CSV formata

        List<String> rowsForCSV = new ArrayList<>();
        for (Item item : filteredGodItems) {
            rowsForCSV.add(item.getPartNumber() + ";"
                    + item.getDescription() + ";"
                    + item.getFunctionGroup() + ";"
                    + item.getSupplierProductGroup() + ";"
                    + item.getUnitOfMeasure() + ";"
                    + item.getBulkQty() + ";"
                    + item.getSupplierNumber() + ";"
                    + item.getMarketingCode() + ";"
                    + item.getFobNetPrice() + ";"
                    + item.getCurrencyCode() + ";"
                    + item.getCountryOfOrigin() + ";"
                    + item.getBlockingCode() + ";"
                    + item.getWeight() + ";"
                    + item.getVolume() + ";"
                    + item.getTariffNo() + ";"
                    + item.getCompanyCode() + ";"
                    + item.getEnvironmentalFee1() + ";"
                    + item.getEnvironmentalFee2() + ";"
                    + item.getEnvironmentalFee3() + ";"
                    + item.getEnvironmentalFee4() + ";"
                    + item.getRetailPrice() + ";"
                    + item.getDealerNetPrice() + ";"
                    + item.getPassiveFlag() + ";"
                    + item.getDiscountCode());
        }
        System.out.println("bandomasis spausdinimas CVS paruosto masyvo " + rowsForCSV);

        //rasom i CSV
        FileWriter writerToCSV = new FileWriter(OUTPUT_PATH);

        for (String csvRow : rowsForCSV) {
            writerToCSV.write(csvRow + "\r");
        }
        writerToCSV.close();
    }
}