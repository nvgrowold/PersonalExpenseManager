package com.example.personalexpensemanager;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;

import com.itextpdf.layout.Document;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FillIRFormActivity extends AppCompatActivity {

    private Spinner spinnerTaxYear;
    private TextView tvUsername, tvTotalIncome, tvTotalExpenses, tvNetIncome, tvTaxPayable;
    private EditText etDividends, etOverseasIncome, etDonations;
    private Button btnSubmit;

    private String userId;
    private double incomeSum = 0;
    private double expenseSum = 0;
    private double taxRate = 0.175; // 17.5% default

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fill_irform);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get userId from intent
        userId = getIntent().getStringExtra("userId");

        spinnerTaxYear = findViewById(R.id.spinner_tax_year);
        tvUsername = findViewById(R.id.tv_username);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpenses = findViewById(R.id.tv_total_expense);
        tvNetIncome = findViewById(R.id.tv_net_income);
        tvTaxPayable = findViewById(R.id.tv_tax_payable);

        etDividends = findViewById(R.id.et_dividends);
        etOverseasIncome = findViewById(R.id.et_overseas_income);
        etDonations = findViewById(R.id.et_donations);

        btnSubmit = findViewById(R.id.btn_submit_ir_form);

        setupTaxYearSpinner();

        btnSubmit.setOnClickListener(v -> submitIRForm());


        Button btnExportPdf = findViewById(R.id.btn_export_pdf);
        btnExportPdf.setOnClickListener(v -> {
            String taxYear = spinnerTaxYear.getSelectedItem().toString();
            generatePdf(taxYear);
        });

        //change user's income, expense, ...information upon selection of different years
        spinnerTaxYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTaxYear = parent.getItemAtPosition(position).toString();
                loadUserData(selectedTaxYear); // reload data
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //load once on launch
        String defaultYear = spinnerTaxYear.getSelectedItem().toString();
        loadUserData(defaultYear);


    }

    private void setupTaxYearSpinner() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) {
            int start = currentYear - i - 1;
            int end = currentYear - i;
            years[i] = start + "/" + end;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_text_item_years, years);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_years);
        spinnerTaxYear.setAdapter(adapter);
    }

    private void loadUserData(String taxYear) {
        db = FirebaseFirestore.getInstance();

        //set username
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvUsername.setText(doc.getString("username"));
            }
        });

        // Get tax year range from spinner string
        String[] parts = taxYear.split("/");
        int startYear = Integer.parseInt(parts[0]);
        int endYear = Integer.parseInt(parts[1]);

        Calendar start = Calendar.getInstance();
        start.set(startYear, Calendar.APRIL, 1); // 1 April start

        Calendar end = Calendar.getInstance();
        end.set(endYear, Calendar.MARCH, 31); // 31 March end

        // Reset values
        incomeSum = 0;
        expenseSum = 0;

        // Fetch transactions
        db.collection("users")
                .document(userId)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("date", start.getTime())
                .whereLessThanOrEqualTo("date", end.getTime())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        String type = doc.getString("transactionType"); // Note: you renamed 'type' to 'transactionType'
                        double amount = doc.getDouble("amount");

                        if ("Income".equalsIgnoreCase(type)) {
                            incomeSum += amount;
                        } else if ("Expense".equalsIgnoreCase(type)) {
                            expenseSum += amount;
                        }
                    }
                    updateCalculatedFields();
                });
    }

    private void updateCalculatedFields() {
        double netIncome = incomeSum - expenseSum;
        double taxPayable = netIncome * taxRate;

        tvTotalIncome.setText("$" + incomeSum);
        tvTotalExpenses.setText("$" + expenseSum);
        tvNetIncome.setText("$" + netIncome);
        tvTaxPayable.setText("$" + taxPayable);
    }

    private void submitIRForm() {
        double dividends = parseDouble(etDividends.getText().toString());
        double overseasIncome = parseDouble(etOverseasIncome.getText().toString());
        double donations = parseDouble(etDonations.getText().toString());

        String taxYear = spinnerTaxYear.getSelectedItem().toString();

        Map<String, Object> form = new HashMap<>();
        form.put("income", incomeSum);
        form.put("expenses", expenseSum);
        form.put("netIncome", incomeSum - expenseSum);
        form.put("dividends", dividends);
        form.put("overseasIncome", overseasIncome);
        form.put("donations", donations);
        form.put("taxPayable", (incomeSum - expenseSum) * taxRate);
        form.put("taxYear", taxYear);
        form.put("submittedAt", new Date());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("irForms")
                .document("form_" + taxYear.replace("/", "_"))
                .set(form);
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    //export pdf file
    private void generatePdf(String taxYear) {
        String fileName = "IR3_Form_" + taxYear.replace("/", "_") + ".pdf";
        File path = new File(getExternalFilesDir(null), fileName);

        try {
            PdfWriter writer = new PdfWriter(path);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("IR3 Individual Income Tax Return")
                    .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\nTax Year: " + taxYear));
            document.add(new Paragraph("Username: " + tvUsername.getText()));
            document.add(new Paragraph("Total Income: $" + incomeSum));
            document.add(new Paragraph("Total Expenses: $" + expenseSum));
            document.add(new Paragraph("Net Income: $" + (incomeSum - expenseSum)));
            document.add(new Paragraph("Tax Payable: $" + ((incomeSum - expenseSum) * taxRate)));

            document.add(new Paragraph("Dividends: $" + etDividends.getText()));
            document.add(new Paragraph("Overseas Income: $" + etOverseasIncome.getText()));
            document.add(new Paragraph("Donations: $" + etDonations.getText()));

            document.add(new Paragraph("\nPrepared by Accountant on: " + new Date().toString()));

            document.close();
            Toast.makeText(this, "PDF saved to: " + path.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to create PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}