package ca.uoit.igorleonardo.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormatSymbols;


public class MyActivity extends Activity {

    private String currentDisplay = "";
    private final String currentDisplayTag = "";
    private String operators = "-+*/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        if (savedInstanceState != null) {
            currentDisplay = savedInstanceState.getString(currentDisplayTag);
            TextView textView = (TextView) findViewById(R.id.display);
            textView.setText(currentDisplay);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putString(currentDisplayTag, currentDisplay);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void buttonPressed(View view) {
        Button bPressed = (Button)view;
        String bValue = bPressed.getText().toString();
        TextView displayText = (TextView) findViewById(R.id.display);

        // "point" control
        if(bValue.equals(".")){
            String[] exprs = getExpressions(currentDisplay);
            String lastExpr = exprs[exprs.length-1];
            if(checkPoint(lastExpr)){
                if(getLastInput().equals("") || operators.contains(getLastInput())){
                    displayText.append("0");
                }
            } else bValue = "";
        }

        // operators control

        /*if( ((lastInput.equals("+") || lastInput.equals("-")) && bValue.equals("-")) ||
                lastInput.equals("+") && bValue.equals("+") ){
            backspace(view);
        } else if((lastInput.equals("-") && bValue.equals("+")) ||
                (lastInput.equals("") && bValue.equals("+"))    ||
                (lastInput.equals(".") && bValue.equals("."))   ){
            bValue = "";
        }*/

        //regex to replace consecutive points, check result

        displayText.append(bValue);
        currentDisplay = displayText.getText().toString();
    }

    public void result(View view) {
        float number1, number2;
        Character operator;
        TextView displayText = (TextView) findViewById(R.id.display);
        String[] exprs = getExpressions(currentDisplay);

        switch(exprs.length){
            case 3:
                // eg.: [1.5, *, 4.2]
                number1 = Float.parseFloat(exprs[0]);
                operator = exprs[1].charAt(0);
                number2 = Float.parseFloat(exprs[2]);
                break;
            case 4:
                // eg.: [1.5, *, -, 4.2]
                number1 = Float.parseFloat(exprs[0]);
                operator = exprs[1].charAt(0);
                number2 = Float.parseFloat(exprs[2] + exprs[3]);
                break;
            case 5:
                // eg.: [, -, 1.5, *, 4.2]
                number1 = Float.parseFloat(exprs[1] + exprs[2]);
                operator = exprs[3].charAt(0);
                number2 = Float.parseFloat(exprs[4]);
                break;
            case 6:
                // eg.: [, -, 1.5, *, -, 4.2]
                number1 = Float.parseFloat(exprs[1] + exprs[2]);
                operator = exprs[3].charAt(0);
                number2 = Float.parseFloat(exprs[4] + exprs[5]);
                break;
            default:
                throw new RuntimeException("Unknow equation format.");
        }

        switch(operator){
            case '+':
                displayText.setText(Float.toString(number1 + number2));
                break;
            case '-':
                displayText.setText(Float.toString(number1-number2));
                break;
            case '*':
                displayText.setText(Float.toString(number1*number2));
                break;
            case '/':
                if(number2 == 0)
                    displayText.setText(DecimalFormatSymbols.getInstance().getInfinity());
                else {
                    displayText.setText(Float.toString(number1 / number2));
                }
                break;
            default:
                throw new RuntimeException("Unknow operator.");
        }

        /*if (value == Math.round(value)) {
            Inteiro
        } else {
            float
        }*/

        currentDisplay = displayText.getText().toString();
    }

    public void backspace(View view) {
        TextView displayText = (TextView) findViewById(R.id.display);

        if(!currentDisplay.equals("")) {
            displayText.setText(currentDisplay.substring(0, currentDisplay.length() - 1));
            currentDisplay = displayText.getText().toString();
            if(currentDisplay.length() == 0) {
                reset(view);
            }
        }
    }

    public void reset(View view) {
        TextView displayText = (TextView) findViewById(R.id.display);
        displayText.setText("");
        currentDisplay = "";
    }

    private String[] getExpressions(String equation){
        String regex = "(?<=op)|(?=op)".replace("op", "["+operators+"]");
        return equation.split(regex);
    }

    private boolean checkPoint(String expr){
        int count = expr.length() - expr.replace(".", "").length();
        return count!=1;
    }

    public String getLastInput(){
        return (currentDisplay.length() >= 1) ? currentDisplay.substring(currentDisplay.length() - 1, currentDisplay.length()) : "";
    }
}
