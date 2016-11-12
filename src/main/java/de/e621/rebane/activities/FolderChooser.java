package de.e621.rebane.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import de.e621.rebane.a621.R;

public class FolderChooser extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static File activeFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "e621"); //Fallback location. Should be unused
    public static String FOLDERINTENTEXTRA = "Folder Chooser Intent Start Folder";
    ListView lv;
    TextView path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_chooser);

        (lv = (ListView) findViewById(R.id.lstFolder)).setOnItemClickListener(this);
        findViewById(R.id.bnApply).setOnClickListener(this);
        //lv.setOnItemClickListener(this);
        path = (TextView) findViewById(R.id.txtPath);

        Intent intent = getIntent();
        String fld = intent.getStringExtra(FOLDERINTENTEXTRA);
        if (fld != null) activeFolder = new File(fld);

        listFiles();
    }

    public void listFiles() {
        if (!activeFolder.exists()) activeFolder.mkdirs();
        File[] folders = activeFolder.listFiles();
        List<String> sfolders = new LinkedList<String>();
        if (activeFolder.getParentFile()!=null) sfolders.add(".. (Parent)");
        if (folders != null) for (File folder : folders) {
            if (folder.isDirectory()) sfolders.add(folder.getName());
        }
        lv.setAdapter(new ArrayAdapter<String>(this, R.layout.singleline_listentry, sfolders));
        path.setText(activeFolder.getAbsolutePath());
    }

    @Override public void onClick(View view) {
        if (view.getId() == R.id.bnApply) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(FOLDERINTENTEXTRA, activeFolder.getAbsolutePath());
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }

    @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.lstFolder) {
            String folder = ((TextView) view).getText().toString();
            Logger.getLogger("a621").info("clicked " + folder + " @" + i);
            if (folder.equals(".. (Parent)") && i == 0)
                activeFolder = activeFolder.getParentFile();
            else activeFolder = new File(activeFolder, folder);
            listFiles();
        }
    }
}
