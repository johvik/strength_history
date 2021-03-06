package strength.history.ui.history;

import java.util.Collection;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import strength.history.R;
import strength.history.data.DataListener;
import strength.history.data.DataProvider;
import strength.history.data.SortedList;
import strength.history.data.provider.WeightProvider;
import strength.history.data.provider.WorkoutDataProvider;
import strength.history.data.provider.WorkoutProvider;
import strength.history.data.structure.Weight;
import strength.history.data.structure.Workout;
import strength.history.data.structure.WorkoutData;
import strength.history.ui.custom.CustomTitleFragmentActivity;
import strength.history.ui.dialog.EventDeleteConfirmDialog;
import strength.history.ui.dialog.WeightDialog;

public class HistoryActivity extends CustomTitleFragmentActivity implements
		WeightProvider.Events, WorkoutDataProvider.Events,
		WorkoutProvider.Events, WeightDialog.Listener,
		EventDeleteConfirmDialog.Listener {
	private static final String SELECTED_INDEX = "seli";
	private HistoryAdapter historyAdapter;
	private SortedList<Workout> workouts = new SortedList<Workout>(
			new Comparator<Workout>() {
				@Override
				public int compare(Workout lhs, Workout rhs) {
					return lhs.compareTo(rhs);
				}
			}, true);
	private SortedList<HistoryEvent> historyEvents = new SortedList<HistoryEvent>(
			new Comparator<HistoryEvent>() {
				@Override
				public int compare(HistoryEvent lhs, HistoryEvent rhs) {
					return lhs.compareTo(rhs);
				}
			}, true);
	private DataProvider dataProvider;
	private boolean workoutsLoaded = false;
	private boolean workoutDataLoaded = false;
	private boolean weightsLoaded = false;
	private View menuItemDelete;
	private View menuItemEdit;
	private int selectedIndex = AdapterView.INVALID_POSITION;
	private ListView listViewHistory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.history);
		setCustomBackButton(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		listViewHistory = (ListView) findViewById(R.id.listViewHistory);
		listViewHistory.setEmptyView(findViewById(R.id.textViewEmptyList));
		historyAdapter = new HistoryAdapter(this, historyEvents, workouts);
		listViewHistory.setAdapter(historyAdapter);
		listViewHistory.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listViewHistory.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedIndex = position;
				updateMenu(true);
			}
		});

		addMenuItem(createMenuItem(R.drawable.ic_action_chart, R.string.charts,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(HistoryActivity.this,
								HistoryChartActivity.class);
						startActivity(i);
					}
				}));
		menuItemDelete = createMenuItem(R.drawable.ic_action_delete,
				R.string.delete_data, new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (selectedIndex != AdapterView.INVALID_POSITION) {
							HistoryEvent e = historyEvents.get(selectedIndex);
							showEventDeleteConfirmDialog(e);
						}
					}
				});
		menuItemEdit = createMenuItem(R.drawable.ic_action_edit,
				R.string.edit_data, new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (selectedIndex != AdapterView.INVALID_POSITION) {
							HistoryEvent e = historyEvents.get(selectedIndex);
							if (e.isWeight()) {
								showWeightDialog(e.getWeight().getWeight());
							} else {
								Intent i = new Intent(HistoryActivity.this,
										WorkoutDataEditActivity.class);
								i.putExtra(
										WorkoutDataEditActivity.WORKOUT_NAME, e
												.getEventString(
														HistoryActivity.this,
														workouts));
								i.putExtra(
										WorkoutDataEditActivity.WORKOUT_DATA, e
												.getWorkoutData().copy());
								startActivity(i);
							}
						}
					}
				});
		if (savedInstanceState != null) {
			selectedIndex = savedInstanceState.getInt(SELECTED_INDEX,
					AdapterView.INVALID_POSITION);
		}
	}

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_history;
	}

	@Override
	protected void onResume() {
		super.onResume();
		historyEvents.clear();
		workouts.clear();
		Context c = getApplicationContext();
		dataProvider = DataListener.add(this);
		workoutsLoaded = false;
		workoutDataLoaded = false;
		weightsLoaded = false;
		updateProgressBar();
		dataProvider.queryWorkout(c);
		dataProvider.queryWorkoutData(c);
		dataProvider.queryWeight(c);
	}

	@Override
	protected void onPause() {
		super.onPause();
		DataListener.remove(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Context c = getApplicationContext();
		dataProvider.stop((Workout) null, c);
		dataProvider.stop((WorkoutData) null, c);
		dataProvider.stop((Weight) null, c);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_INDEX, selectedIndex);
	}

	private void showWeightDialog(double weight) {
		FragmentManager fm = getSupportFragmentManager();
		WeightDialog d = new WeightDialog();
		Bundle b = new Bundle();
		b.putDouble(WeightDialog.WEIGHT, weight);
		d.setArguments(b);
		d.show(fm, "fragment_weight_dialog_edit");
	}

	private void showEventDeleteConfirmDialog(HistoryEvent e) {
		FragmentManager fm = getSupportFragmentManager();
		EventDeleteConfirmDialog d = new EventDeleteConfirmDialog();
		Bundle b = new Bundle();
		b.putString(EventDeleteConfirmDialog.NAME,
				e.getEventString(this, workouts));
		b.putLong(EventDeleteConfirmDialog.TIME, e.getTime());
		d.setArguments(b);
		d.show(fm, "fragment_entry_delete_confirm");
	}

	private void updateProgressBar() {
		if (workoutsLoaded && workoutDataLoaded && weightsLoaded) {
			updateMenu(selectedIndex != AdapterView.INVALID_POSITION);
			setCustomProgressBarVisibility(false);
			listViewHistory.setItemChecked(selectedIndex, true);
			listViewHistory.setSelection(selectedIndex);
		} else {
			setCustomProgressBarVisibility(true);
		}
	}

	private void updateMenu(boolean show) {
		removeMenuItem(menuItemDelete);
		removeMenuItem(menuItemEdit);
		if (show) {
			addMenuItem(menuItemDelete, 0);
			addMenuItem(menuItemEdit, 0);
		}
	}

	@Override
	public void deleteCallback(Workout e, boolean ok) {
		if (ok) {
			workouts.remove(e);
		}
	}

	@Override
	public void insertCallback(Workout e, boolean ok) {
		if (ok) {
			workouts.add(e);
		}
	}

	@Override
	public void updateCallback(Workout old, Workout e, boolean ok) {
		if (ok) {
			workouts.remove(old);
			workouts.add(e);
		}
	}

	@Override
	public void workoutQueryCallback(Collection<Workout> e, boolean done) {
		workouts.addAll(e);
		historyAdapter.notifyDataSetChanged();
		if (done) {
			workoutsLoaded = true;
			updateProgressBar();
		}
	}

	@Override
	public void deleteCallback(WorkoutData e, boolean ok) {
		if (ok) {
			historyEvents.remove(new HistoryEvent(e));
			historyAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void insertCallback(WorkoutData e, boolean ok) {
		if (ok) {
			historyEvents.add(new HistoryEvent(e));
			historyAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void updateCallback(WorkoutData old, WorkoutData e, boolean ok) {
		if (ok) {
			historyEvents.remove(new HistoryEvent(old));
			historyEvents.add(new HistoryEvent(e));
			historyAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void workoutDataQueryCallback(Collection<WorkoutData> e, boolean done) {
		for (WorkoutData d : e) {
			historyEvents.add(new HistoryEvent(d));
		}
		historyAdapter.notifyDataSetChanged();
		if (done) {
			workoutDataLoaded = true;
			updateProgressBar();
		}
	}

	@Override
	public void deleteCallback(Weight e, boolean ok) {
		if (ok) {
			historyEvents.remove(new HistoryEvent(e));
			historyAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void insertCallback(Weight e, boolean ok) {
		if (ok) {
			historyEvents.add(new HistoryEvent(e));
			historyAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void updateCallback(Weight old, Weight e, boolean ok) {
		if (ok) {
			historyEvents.remove(new HistoryEvent(old));
			historyEvents.add(new HistoryEvent(e));
			historyAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void weightQueryCallback(Collection<Weight> e, boolean done) {
		for (Weight w : e) {
			historyEvents.add(new HistoryEvent(w));
		}
		historyAdapter.notifyDataSetChanged();
		if (done) {
			weightsLoaded = true;
			updateProgressBar();
		}
	}

	@Override
	public void onWeightOk(double weight) {
		if (selectedIndex != AdapterView.INVALID_POSITION) {
			HistoryEvent e = historyEvents.get(selectedIndex);
			if (e.isWeight()) {
				Weight w = e.getWeight();
				w.setWeight(weight);
				dataProvider.update(w, getApplicationContext());
			}
		}
	}

	@Override
	public void onWeightCancel(double weight) {
		// Do nothing
	}

	@Override
	public void onEventDeleteConfirm() {
		if (selectedIndex != AdapterView.INVALID_POSITION) {
			HistoryEvent e = historyEvents.remove(selectedIndex);
			if (e.isWeight()) {
				dataProvider.delete(e.getWeight(), getApplicationContext());
			} else {
				dataProvider
						.delete(e.getWorkoutData(), getApplicationContext());
			}
			selectedIndex = AdapterView.INVALID_POSITION;
			listViewHistory.clearChoices();
			updateMenu(false);
		}
	}
}
