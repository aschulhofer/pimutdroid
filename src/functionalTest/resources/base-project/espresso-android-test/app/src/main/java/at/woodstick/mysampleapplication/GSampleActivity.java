package at.woodstick.mysampleapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GSampleActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "at.woodstick.intent.key.extra_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsample);
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        final TextView messageLabel = (TextView) findViewById(R.id.messageInputTextView);
        final EditText editText = (EditText) findViewById(R.id.editText);
        final Message message = new Message(editText.getText().toString());

        if(message.isAvailable()) {
            messageLabel.setVisibility(View.INVISIBLE);

            intent.putExtra(EXTRA_MESSAGE, message.getMessage());
            startActivity(intent);
        } else {
            messageLabel.setVisibility(View.VISIBLE);
        }
    }

    public class Message {
        private final String message;
        private final MsgHolder msgHolder;

        public Message(String message) {
            this.message = message;
            msgHolder = new MsgHolder(message);
        }

        public boolean isAvailable() {
            return ( (message != null) && !message.isEmpty() );
        }

        public String getMessage() {
            return message;
        }

        public String emptyIfNull() {
            return msgHolder.emptyIfNull();
        }

        public class MsgHolder {
            private final String message;

            public MsgHolder(String message) {
                this.message = message;
            }

            public String emptyIfNull() {
                return this.message == null ? "" : this.message;
            }
        }
    }
}
