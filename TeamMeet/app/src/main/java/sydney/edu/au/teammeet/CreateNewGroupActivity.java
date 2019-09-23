package sydney.edu.au.teammeet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class CreateNewGroupActivity extends BaseActivity {

    private EditText editText;
    private Button backToGroups;
    private Button addToDatabase; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_group);
    }
}
