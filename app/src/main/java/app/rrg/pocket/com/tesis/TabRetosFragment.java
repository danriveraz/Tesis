package app.rrg.pocket.com.tesis;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabRetosFragment  extends Fragment {
    private static final String TAG = "TabRetosFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tabretos_fragment, container,false);
        return view;
    }
}
