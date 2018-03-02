package info.dourok.esactivity.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/** A simple {@link Fragment} subclass. */
public class OneButtonSupportFragment extends Fragment {

  private CaptureTestActivity activity;
  private String btnText;

  public OneButtonSupportFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of this fragment using the provided
   * parameters.
   */
  public static OneButtonSupportFragment newInstance(String btnText) {
    Bundle arguments = new Bundle();
    OneButtonSupportFragment fragment = new OneButtonSupportFragment();
    arguments.putString("btnText", btnText);
    fragment.setArguments(arguments);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() == null) {
      btnText = "captureSupportFragmentWithId";
    } else {
      btnText = getArguments().getString("btnText");
    }
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Button btn = new Button(getActivity());
    btn.setText(btnText);
    btn.setOnClickListener(
        v -> EditorActivityBuilder.create(activity).forContent(this::showContent).start());
    return btn;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void showContent(String s) {
    activity.showContent(s);
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
