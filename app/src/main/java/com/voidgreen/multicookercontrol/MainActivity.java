package com.voidgreen.multicookercontrol;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Vibrator;

public class MainActivity extends Activity implements View.OnClickListener {

    //Экземпляры классов наших кнопок
    Button redButton;
    Button greenButton;
    Button StopButton;
    Button RiceButton;
    Button FryButton;
    private Vibrator myVib;
    //Сокет, с помощью которого мы будем отправлять данные на Arduino
    BluetoothSocket clientSocket;

    //Эта функция запускается автоматически при запуске приложения
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //"Соединям" вид кнопки в окне приложения с реализацией
        redButton = (Button) findViewById(R.id.toggleRedLed);
        greenButton = (Button) findViewById(R.id.toggleGreenLed);
        StopButton = (Button) findViewById(R.id.Button01);
        RiceButton = (Button) findViewById(R.id.Button02);
        FryButton = (Button) findViewById(R.id.Button03);


        //Добавлем "слушатель нажатий" к кнопке
        redButton.setOnClickListener(this);
        greenButton.setOnClickListener(this);
        StopButton.setOnClickListener(this);
        RiceButton.setOnClickListener(this);
        FryButton.setOnClickListener(this);
        myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        //Включаем bluetooth. Если он уже включен, то ничего не произойдет
        String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
        startActivityForResult(new Intent(enableBT), 0);

        //Мы хотим использовать тот bluetooth-адаптер, который задается по умолчанию
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        //Пытаемся проделать эти действия
        try {
            //Устройство с данным адресом - наш Bluetooth Bee
            //Адрес опредеяется следующим образом: установите соединение
            //между ПК и модулем (пин: 1234), а затем посмотрите в настройках
            //соединения адрес модуля. Скорее всего он будет аналогичным.
            BluetoothDevice device = bluetooth.getRemoteDevice("98:D3:31:30:18:59");

            //Инициируем соединение с устройством
            //  Method m = device.getClass().getMethod(
            // "createRfcommSocket", new Class[] {int.class});

            // clientSocket = (BluetoothSocket) m.invoke(device, 1);
            clientSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            clientSocket.connect();

            //В случае появления любых ошибок, выводим в лог сообщение
        } catch (IOException e) {
            Log.d("BLUETOOTH", e.getMessage());
        } catch (SecurityException e) {
            Log.d("BLUETOOTH", e.getMessage());

        } catch (IllegalArgumentException e) {
            Log.d("BLUETOOTH", e.getMessage());

        }

        //Выводим сообщение об успешном подключении
        Toast.makeText(getApplicationContext(), "CONNECTED", Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //Как раз эта функция и будет вызываться

    @Override
    public void onClick(View v) {
        //Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
        //Пытаемся послать данные
        try {
            //Получаем выходной поток для передачи данных
            OutputStream outStream = clientSocket.getOutputStream();

            byte[] value = new byte[]{0x00, 0x0A, 0x0D};
            String cvs = "00SetCP105020120001\r\n";


            //В зависимости от того, какая кнопка была нажата,
            //изменяем данные для посылки
            if (v.getId() == R.id.toggleRedLed) {
                //Toast.makeText(this, "toggleRedLed", Toast.LENGTH_LONG).show();
                value[0] = 'D';
                cvs = "00SetCP120020105001\r\n";
                myVib.vibrate(50);
            } else if (v.getId() == R.id.toggleGreenLed) {
                //Toast.makeText(this, "toggleGreenLed", Toast.LENGTH_LONG).show();
                value[0] = 'U';
                cvs = "00SetCP110020105001\r\n";
                myVib.vibrate(50);
            } else if (v.getId() == R.id.Button01) {
                //Toast.makeText(this, "toggleGreenLed", Toast.LENGTH_LONG).show();
                value[0] = 'U';
                cvs = "00SetCP011001011001\r\n";
                myVib.vibrate(50);
            } else if (v.getId() == R.id.Button02) {
                //Toast.makeText(this, "toggleGreenLed", Toast.LENGTH_LONG).show();
                value[0] = 'U';
                cvs = "00SetCP100008110001\r\n";
                myVib.vibrate(50);
            } else if (v.getId() == R.id.Button03) {
                //Toast.makeText(this, "toggleGreenLed", Toast.LENGTH_LONG).show();
                value[0] = 'U';
                cvs = "00SetCP18010180001\r\n";
                myVib.vibrate(50);
            }

            // byte b = (byte)value;
            //Пишем данные в выходной поток
            outStream.write(cvs.getBytes());
            //Toast.makeText(this, b, Toast.LENGTH_LONG).show();
            // Toast.makeText(this, value[0], Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            //Если есть ошибки, выводим их в лог
            Log.d("BLUETOOTH", e.getMessage());
        }
    }
}