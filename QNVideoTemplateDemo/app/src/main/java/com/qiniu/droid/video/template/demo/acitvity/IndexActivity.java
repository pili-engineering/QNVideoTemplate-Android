package com.qiniu.droid.video.template.demo.acitvity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.fragment.TemplateDetailFragment;
import com.qiniu.droid.video.template.demo.fragment.TemplateListFragment;
import com.qiniu.droid.video.template.demo.model.Template;


public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_index);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, new TemplateListFragment())
                .commit();
    }

    public void detail(Template template) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(
                        R.anim.template_slide_in,  // enter
                        R.anim.template_fade_out,  // exit
                        R.anim.template_fade_in,   // popEnter
                        R.anim.template_slide_out  // popExit
                )
                .replace(R.id.fl_container, TemplateDetailFragment.newInstance(template))
                .commit();
    }

}
