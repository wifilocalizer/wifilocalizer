package de.kk.wifilocalizer.ui.fragments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;

import de.kk.wifilocalizer.R;
import de.kk.wifilocalizer.ui.activities.MainActivity;
import de.kk.wifilocalizer.ui.models.MapImage;

/**
 * Handles Maps screen
 * <p />
 * Activities containing this fragment MUST implement the {@link MapsCallbacks} interface.
 */
public class MapsFragment extends Fragment implements AbsListView.OnItemClickListener {
    private static final String INIT_STRING = "maps_initstring";
    private static final int SELECT_IMAGE_AS_MAP = 100;
    private static final String fileName = "MapList.db";
    private static ArrayList<MapImage> MAPS;

    @SuppressWarnings("unused")
    private String mInitString;

    private MapsCallbacks mCallbacks;
    private AbsListView mListView;
    private ListAdapter mAdapter;

    public MapsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using the provided parameters
     * 
     * @return A new instance of fragment LocalizationFragment
     */
    public static MapsFragment newInstance(String initString) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString(INIT_STRING, initString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mInitString = getArguments().getString(INIT_STRING);
        }

        mAdapter = new ArrayAdapter<MapImage>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, MAPS);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so you can be notified on item clicks
        mListView.setOnItemClickListener(this);

        getActivity().setTitle(getString(R.string.maps_fragment));
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
        case R.id.action_add:
            Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
            imagePickerIntent.setType("image/*");
            startActivityForResult(imagePickerIntent, SELECT_IMAGE_AS_MAP);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
        case SELECT_IMAGE_AS_MAP:
            // Adds chosen image to maps list
            if (resultCode == android.app.Activity.RESULT_OK) {
                final String filepath = data.getDataString();
                final EditText input = new EditText(getActivity());
                input.setHint("Mapname");
                new AlertDialog.Builder(getActivity()).setTitle(R.string.mapsfragment_alert_dialog_add_map_title)
                        .setMessage(R.string.mapsfragment_alert_dialog_add_map_message).setView(input)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String filename;
                                if (input.getText().toString().equals(""))
                                    filename = "Map " + String.valueOf(MAPS.size() + 1);
                                else
                                    filename = input.getText().toString();
                                MAPS.add(new MapImage(String.valueOf(System.currentTimeMillis()), filepath, filename));
                                ((BaseAdapter) mAdapter).notifyDataSetChanged();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Close dialog
                            }
                        }).show();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (MapsCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onMapSelected");
        }

        load(activity);
    }

    @SuppressWarnings("unchecked")
    private void load(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean(MainActivity.MAPS_LIST_STORED, false)) {
            FileInputStream fis;
            try {
                fis = context.openFileInput(fileName);
                ObjectInputStream is = new ObjectInputStream(fis);
                MAPS = (ArrayList<MapImage>) is.readObject();
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            MAPS = new ArrayList<MapImage>();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        store(getActivity());
        mCallbacks = null;
    }

    private void store(Context context) {
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(MAPS);
            os.close();
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            sp.edit().putBoolean(MainActivity.MAPS_LIST_STORED, true).commit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mCallbacks) {
            // Passes name and filepath to callback method
            mCallbacks.onMapSelected(MAPS.get(position).getId(), MAPS.get(position).getFilepath());
        }
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an interaction in this fragment
     * to be communicated to the activity and potentially other fragments contained in that activity.
     * <p/>
     * See the Android Training lesson <a href= "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface MapsCallbacks {
        public void onMapSelected(String mapname, String filepath);
    }
}
