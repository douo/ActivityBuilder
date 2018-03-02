package info.dourok.esactivity.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/** A simple {@link Fragment} subclass. */
public class OneButtonFragment extends Fragment {

  CaptureTestActivity activity;

  public OneButtonFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of this fragment using the provided
   * parameters.
   */
  public static OneButtonFragment newInstance() {
    return new OneButtonFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Button btn = new Button(getActivity());
    btn.setText("captureFragment");
    btn.setOnClickListener(
        v -> EditorActivityBuilder.create(activity).forContent(activity::showContent).start());
    return btn;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof CaptureTestActivity) {
      activity = (CaptureTestActivity) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement CaptureTestActivity");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    activity = null;
  }
}
