package com.quickblox.snippets;

import android.content.Context;
import android.widget.Toast;
import com.quickblox.core.result.Result;

import java.util.ArrayList;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 11:02
 */
public class Snippets {

    protected Context context;
    protected ArrayList<Snippet> snippets = new ArrayList<Snippet>();

    public void printResultToConsole(Result result) {
        String message = "";
        if (result.isSuccess()) {
            message = "[OK] Result is successful! You can cast result to specific result and extract data.";
            Toast.makeText(context, "[OK] Result is successful!", Toast.LENGTH_SHORT).show();
        } else {
            message = String.format("[ERROR %s] Request has been completed with errors: %s",
                    result.getStatusCode(), result.getErrors());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        System.out.println(message);
    }

    public void handleErrors(Result result) {
        String message = String.format("[ERROR %s] Request has been completed with errors: %s",
                result.getStatusCode(), result.getErrors());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        // print
        System.out.println(message);
    }


    public Snippets(Context context) {
        this.context = context;
    }

    public ArrayList<Snippet> getSnippets() {
        return snippets;
    }

    public void setSnippets(ArrayList<Snippet> snippets) {
        this.snippets = snippets;
    }

}