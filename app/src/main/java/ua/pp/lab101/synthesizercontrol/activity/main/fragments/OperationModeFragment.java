package ua.pp.lab101.synthesizercontrol.activity.main.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import ua.pp.lab101.synthesizercontrol.R;
import ua.pp.lab101.synthesizercontrol.activity.main.MainActivity;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OperationModeFragment.OperationModeListener}
 * interface.
 */
public class OperationModeFragment extends ListFragment {

    private OperationModeListener mListener;
    private int currentIndex = -1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OperationModeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.list_main_modes, MainActivity.ModesTitleArray));
        // If a title has already been selected in the past, reset the selection state now
//        if (currentIndex != MainActivity.UNSELECTED) {
//            setSelection(currentIndex);
//        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OperationModeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        currentIndex = position;

        // Indicates the selected item has been checked
        getListView().setItemChecked(position, true);

        // Inform the QuoteViewerActivity that the item in position pos has been selected
        mListener.onModeSelected(position);
    }


    public interface OperationModeListener {
        // TODO: Update argument type and name
        public void onModeSelected(int currentIndex);
    }

}
