package net.handsmidia.blink102.view;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.handsmidia.blink102.R;
import net.handsmidia.blink102.utilities.Utils;

import java.util.ArrayList;

public class TelaWhats extends AppCompatActivity {

    private RelativeLayout mLoading;
    private ImageView mImagem;
    private Context mContext;
    private Utils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_whats);

        mLoading = (RelativeLayout) findViewById(R.id.loading_view_aguarde);
        mImagem = (ImageView) findViewById(R.id.iv_tela_fale_conosco);
        mContext = this;
        mUtils = new Utils(mContext);
        mUtils.permissaoAcessarContatos(this);
        mImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showLoading();

                String value = mUtils.getPref(Utils.PERGUNTARNOVAMENTE);
                if (mUtils.verificarContatoNaAgenda().contains(Utils.NOMERADIO) || value != null) {
                    hideLoading();
                    mUtils.abrirWhatsapp(Utils.NUMERORADIO);
                } else {
                    hideLoading();
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Adicionar contato");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Deseja adicionar Blink 102 na sua lista de contatos?");
                    builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showLoading();
                            criarContatoNaAgenda(Utils.NOMERADIO, Utils.NUMERORADIO);
                            mUtils.putPref(Utils.EXISTE, "contatoExiste");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                    mUtils.abrirWhatsapp(Utils.NUMERORADIO);
                                }
                            }, 1000);
                        }
                    });
                    builder.setNegativeButton("Nāo", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mUtils.abrirWhatsapp(Utils.NUMERORADIO);
                            mUtils.putPref(Utils.PERGUNTARNOVAMENTE, "perguntarNomenteOk");
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    public void criarContatoNaAgenda(String displayName, String number) {
        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
        int contactIndex = cntProOper.size();
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, contactIndex)
                .withValue(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "+" + number)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper);
        } catch (RemoteException exp) {
            Log.d("INSERT CONTATO", exp.getMessage());
        } catch (OperationApplicationException exp) {
            Log.d("INSERT CONTATO", exp.getMessage());
        }
        hideLoading();
        Toast.makeText(mContext, "Contato adicionado com sucesso!", Toast.LENGTH_LONG).show();
    }

    public void showLoading() {
        mLoading.setVisibility(View.VISIBLE);
        mImagem.setVisibility(View.GONE);
    }

    public void hideLoading() {
        mLoading.setVisibility(View.GONE);
        mImagem.setVisibility(View.VISIBLE);
    }
}
