package com.example.pulse.pulseapplication;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionsMainActivity extends AppCompatActivity {

    Spinner spinner;
    Button insert;
    Button select;
    EditText question;
    int i;
    public static final String PREFS_NAME = "PulseTeam";
    ArrayAdapter<ExcelDetail> dataAdapter;
    List<ExcelDetail> excelDetails;
    String json;
    Gson gson;
    Type type;
    ExcelDetail detail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions_main);
        setTitle("JDI IT Pulse");

        spinner = findViewById(R.id.loginSpinner);
        question = findViewById(R.id.question);
        insert = findViewById(R.id.insert);
        select = findViewById(R.id.select);
        final SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!question.getText().toString().trim().equals("")) {
                    i = sharedPreferences.getInt("sheetNumber", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("sheetNumber", ++i);
                    insertInList(editor, sharedPreferences);
                    editor.commit();
                    readExcelFile(QuestionsMainActivity.this, "Test1.xls");
                } else {
                    Toast.makeText(QuestionsMainActivity.this, "Please insert question", Toast.LENGTH_SHORT).show();
                }
            }
        });

        json = sharedPreferences.getString("List", null);
        gson = new Gson();
        type = new TypeToken<ArrayList<ExcelDetail>>() {
        }.getType();
        excelDetails = gson.fromJson(json, type);
        if (excelDetails == null) {
            excelDetails = new ArrayList<>();
            excelDetails.add(createExcelSheet("Please select Question", "Sheet0"));
        }

        loadDropDownList();

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    detail = (ExcelDetail) spinner.getSelectedItem();
                    if (!detail.getSheetName().equals("Sheet0")) {
                        Toast.makeText(QuestionsMainActivity.this, "Selected Item  " + detail.getSheetName(), Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String selected = gson.toJson(detail);
                        editor.putString("selected", selected);
                        insertInList(editor, sharedPreferences);
                        spinner.setSelection(dataAdapter.getPosition(detail));
                        editor.commit();
                    } else {
                        Toast.makeText(QuestionsMainActivity.this, "Please select question", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void loadDropDownList() {
        dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, excelDetails);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    void insertInList(SharedPreferences.Editor editor, SharedPreferences sharedPreferences) {
        json = sharedPreferences.getString("List", null);
        excelDetails.add(createExcelSheet(question.getText().toString(), "Sheet" + i));
        editor.putString("List", gson.toJson(excelDetails));

        loadDropDownList();
    }

    private ExcelDetail createExcelSheet(String question, String sheetName) {
        ExcelDetail excelDetail = new ExcelDetail();
        excelDetail.setQuestion(question);
        excelDetail.setSheetName(sheetName);
        return excelDetail;
    }

    private void readExcelFile(Context context, String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("LOG", "Storage not available or read only");
            return;
        }

        try {
            File file = new File(context.getExternalFilesDir(null), filename);
            FileInputStream myInput = new FileInputStream(file);

            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            Sheet sheet1 = myWorkBook.createSheet("Sheet" + i);

            Row row1 = sheet1.createRow(0);
            Cell c1 = row1.createCell(0);
            c1.setCellValue(question.getText().toString());

            sheet1.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            Row row2 = sheet1.createRow(1);
            Cell c = row2.createCell(0);
            c.setCellValue("Racf-IDs");

            c = row2.createCell(1);
            c.setCellValue("Response");

            sheet1.setColumnWidth(0, (15 * 500));
            sheet1.setColumnWidth(1, (15 * 500));

            FileOutputStream os = null;
            try {
                os = new FileOutputStream(file);
                myWorkBook.write(os);
                Log.w("FileUtils", "Writing file" + file);
            } catch (IOException e) {
                Log.w("FileUtils", "Error writing " + file, e);
            } catch (Exception e) {
                Log.w("FileUtils", "Failed to save file", e);
            } finally {
                try {
                    if (null != os)
                        os.close();
                } catch (Exception ex) {
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            saveExcelFile(context, filename);
        }

        return;
    }


    private boolean saveExcelFile(Context context, String fileName) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("LOG", "Storage not available or read only");
            return false;
        }

        boolean success = false;

        Workbook wb = new HSSFWorkbook();

        Sheet sheet1 = wb.createSheet("Sheet" + i);

        Row row1 = sheet1.createRow(0);
        Cell c1 = row1.createCell(0);
        c1.setCellValue(question.getText().toString());

        sheet1.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        Row row2 = sheet1.createRow(1);
        Cell c = row2.createCell(0);
        c.setCellValue("Racf-IDs");

        c = row2.createCell(1);
        c.setCellValue("Response");

        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));

        File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }

    public boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}