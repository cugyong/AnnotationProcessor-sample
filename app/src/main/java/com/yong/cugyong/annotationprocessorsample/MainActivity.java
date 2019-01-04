package com.yong.cugyong.annotationprocessorsample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.yong.cugyong.annotation.AnnotationTest;
import com.yong.cugyong.testlib.ProxyMethodTest;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProxyMethodTest.test(MainActivity.this);
            }
        });
    }

    @AnnotationTest
    public void testAnnotation(){
        Toast.makeText(this, "you have clicked the hello world button! ",
                Toast.LENGTH_SHORT).show();
    }
}
