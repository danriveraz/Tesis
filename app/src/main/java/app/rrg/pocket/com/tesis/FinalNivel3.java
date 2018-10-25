package app.rrg.pocket.com.tesis;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.rrg.pocket.com.tesis.Entities.Palabra;
import app.rrg.pocket.com.tesis.Entities.Usuario;
import app.rrg.pocket.com.tesis.Utilidades.PalabraDB;
import app.rrg.pocket.com.tesis.Utilidades.UsuarioDB;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class FinalNivel3 extends AppCompatActivity implements TextToSpeech.OnInitListener, edu.cmu.pocketsphinx.RecognitionListener {

    String idPalabra;
    private PalabraDB dbP;
    private UsuarioDB db;
    Usuario usuario;
    Palabra palabra;
    TextToSpeech tts;
    private SpeechRecognizer recognizer;
    String text;
    boolean acierto;
    List<String> listado;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(this,this);

        recibirId();

        dbP = new PalabraDB(FinalNivel3.this);
        db = new UsuarioDB(FinalNivel3.this);
        acierto=false;
        try {
            listar();
        } catch (IOException e) {
            e.printStackTrace();
        }

        usuario = db.buscarUsuarios(1);
        palabra = dbP.buscarPalabra(Integer.parseInt(idPalabra));

        setContentView(R.layout.activity_finalnivel3);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupActionBar();
        configuracion();

        findViewById(R.id.buttonFN3).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView textViewEscucha = (TextView)findViewById(R.id.textView_escuchaN3);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        recognizer.stop();
                        textViewEscucha.setText("");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        try {
                            Assets assets = new Assets(getApplicationContext());
                            File assetDir = assets.syncAssets();
                            setupRecognizer(assetDir);
                            recognizer.startListening("frases");
                            textViewEscucha.setText("Escuchando...");
                        }catch (IOException e){
                            Toast.makeText(getBaseContext(),"Failed to init recognizer " + e,Toast.LENGTH_LONG).show();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private void setupActionBar(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            //Toast.makeText(this, "Hola", Toast.LENGTH_SHORT).show();
        }
    }

    private void recibirId(){
        Bundle extras = getIntent().getExtras();
        idPalabra = extras.getString("idPalabra");
        Log.d("FinalN3Activity -> ", "Id palabra: " + idPalabra);
    }

    private void configuracion(){
        TextView textView = (TextView) findViewById(R.id.textViewFN3);
        ImageView img = (ImageView) findViewById(R.id.imageViewFinalNivel3);
        textView.setText(palabra.getNombre());

        TextView textViewTusPuntos = (TextView) findViewById(R.id.textViewPuntajeJugadorN3);
        TextView textViewAGanar = (TextView) findViewById(R.id.textViewPuntosAGanarPalabraN3);

        textViewTusPuntos.setText("Tienes: " + usuario.getPuntaje() +", Pts");
        textViewAGanar.setText("Otorga: " + palabra.getPuntaje() +", Pts");

        if(usuario.getTamano().equals("pequeno")){
            textView.setTextSize(36);
            textViewTusPuntos.setTextSize(22);
            textViewAGanar.setTextSize(22);
        } else if (usuario.getTamano().equals("mediano")) {
            textView.setTextSize(40);
            textViewTusPuntos.setTextSize(23);
            textViewAGanar.setTextSize(23);
        }else{
            textView.setTextSize(44);
            textViewTusPuntos.setTextSize(24);
            textViewAGanar.setTextSize(24);
        }

        setImg(palabra.getNombre());
        if(img.getDrawable() == null){
            img.setImageResource(R.drawable.sonido);
        }
    }

    public void setImg(final String palabra){
        ImageView img = (ImageView) findViewById(R.id.imageViewFinalNivel3);

        switch (palabra) {
            case "Trabajo en grupo":
                img.setImageResource(R.drawable.administracion_trabajoengrupo);
                break;
            case "Perro caliente":
                img.setImageResource(R.drawable.comida_perrocaliente);
                break;
            case "Tenis de mesa":
                img.setImageResource(R.drawable.deporte_tenisdemesa);
                break;
            case "Ciencias sociales":
                img.setImageResource(R.drawable.docencia_cienciassociales);
                break;
            case "Tomate de árbol":
                img.setImageResource(R.drawable.frutas_tomatedearbol);
                break;
            case "Hilo dental":
                img.setImageResource(R.drawable.implementospersonal_hilo);
                break;
            case "Crema dental":
                img.setImageResource(R.drawable.implementospersonal_crema);
                break;
            case "Arroz con pollo":
                img.setImageResource(R.drawable.platos_arrozconpollo);
                break;
            case "Bandeja paisa":
                img.setImageResource(R.drawable.platos_bandejapaisa);
                break;
        }

        Button sonido = (Button) findViewById(R.id.buttonSound3);
        sonido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakOut();
            }
        });
    }

    public void onInit(int status){

        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.getDefault());
            if(result == TextToSpeech.LANG_NOT_SUPPORTED ||
                    result == TextToSpeech.LANG_MISSING_DATA){
                Log.e("TTS", "Este lenguaje no es soportado");
            }else{
                //speakOut();
            }
        }else{
            Log.e("TTS", "Inicialización del lenguaje fallida");
        }

    }

    public void speakOut(){
        tts.speak(palabra.getNombre(), TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if(hypothesis == null)
            return;
        //Obtenemos el String de la Hypothesiss

        text = hypothesis.getHypstr();

        //Reiniciamos el reconocedor, de esta forma reconoce voz de forma continua y limpia el buffer
        resetRecognizer();
    }

    public void listar()throws IOException {
        listado = new ArrayList<String>();
        String linea;

        InputStream is = this.getResources().openRawResource(R.raw.nivel3);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        if(is!=null){
            while ((linea = reader.readLine()) != null){
                listado.add(linea);
            }
        }
        is.close();
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if(text != null){
            for(int i=0;i<listado.size();i++){
                if(palabra.getNombre().contains(listado.get(i).split(";")[1])) {
                    if (text.contains(listado.get(i).split(";")[0]))
                        acierto = true;
                }
            }
        }else {
            acierto=false;
        }

        TextView textView = (TextView) findViewById(R.id.textView_escuchaN3);
        if(acierto){
            textView.setText("¡¡Dijiste la palabra!!");
            palabraReconocida();
        }else{
            textView.setText("¡Vuelve a intentarlo!");
        }
        text=null;
        acierto=false;
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }

    private void setupRecognizer(File assetsDir) throws IOException {
        //Esta es la configuración básica, donde se le indican las bibliotecas con las palabras en español
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "es-ptm"))
                .setDictionary(new File(assetsDir, "es.dict"))
                .getRecognizer();
        recognizer.addListener(this);

        //Aquí indicamos el archivo que contiene las palabras clave que queremos reconocer
        // para realizar diferentes acciones. En este caso yo creo un archivo llamado "keys.gram"
        File keysGrammar = new File(assetsDir, "nivel3.gram");
        recognizer.addKeywordSearch("frases",keysGrammar);
    }

    private void resetRecognizer(){
        if(recognizer!=null) {
            recognizer.cancel();
            recognizer.startListening("frases");
        }
    }

    public void palabraReconocida(){
        TextView textViewTusPuntos = (TextView) findViewById(R.id.textViewPuntajeJugadorN3);
        usuario.setPuntaje(palabra.getPuntaje() + usuario.getPuntaje());
        db.updateUsuario(usuario);
        textViewTusPuntos.setText("Tienes: " + usuario.getPuntaje() +", Pts");
    }
}
