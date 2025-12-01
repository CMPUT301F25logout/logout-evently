package com.example.evently.ui.organizer;

import static com.example.evently.utils.DateTimeUtils.toEpochMillis;
import static com.example.evently.utils.DateTimeUtils.treatAsLocalDate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.loadingindicator.LoadingIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.Timestamp;

import com.example.evently.R;
import com.example.evently.data.EventsDB;
import com.example.evently.data.generic.Promise;
import com.example.evently.data.model.Category;
import com.example.evently.data.model.Event;
import com.example.evently.databinding.FragmentCreateEventBinding;
import com.example.evently.utils.FirebaseAuthUtils;

/**
 * Fragment that collects input to create a new organizer-owned {@link Event}
 * <p>
 * It inflates {@code R.layout.fragment_create_event}, performs simple client-side
 * validation, and on success constructs an {@link Event} object and returns it to
 * the previous fragment via the Navigation component's {@code SavedStateHandle}
 * under the key {@code "new_event"}, then calls {@code navigateUp()} to return.
 * Persistence is handled by the receiving screen.
 */
public class CreateEventFragment extends Fragment {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private FragmentCreateEventBinding binding;

    private Uri imageUri;
    private ImageButton imageButton;
    private Category selectedCategory = Category.Other;

    // The following code defines a launcher to pick a picture. For more details, see the android
    // photo picker docs:
    // https://developer.android.com/training/data-storage/shared/photo-picker
    private final ActivityResultLauncher<PickVisualMediaRequest> pickPoster =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    Glide.with(getContext()).load(imageUri).into(imageButton);
                } else {
                    Log.d("Poster Picker", "No poster selected");
                }
            });

    /**
     * Inflates the "Create Event" form
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the view of the inflated form
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateEventBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    /**
     * Builds up a form, validates input, builds an {@link Event}, returns it, and navigates up
     *
     * @param v The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        final var btnCreate = binding.btnCreate;

        binding.btnCancel.setOnClickListener(
                _x -> NavHostFragment.findNavController(this).navigateUp());

        imageButton = v.findViewById(R.id.btnPickPoster);

        // Launches the poster picker when clicked.
        imageButton.setOnClickListener(view -> {
            pickPoster.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        binding.etWinners.setOnEditorActionListener((ignored, actionId, ignored_) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etWinners.clearFocus();
            }
            return false;
        });

        binding.etSelectionDeadline.setOnClickListener(
                ignored -> setupDatePicker("Selection deadline", binding.etSelectionDeadline));
        binding.etEventDate.setOnClickListener(
                ignored -> setupDatePicker("Event date", binding.etEventDate));

        setupEventTimePicker();

        btnCreate.setOnClickListener(_x -> {
            String name = binding.etEventName.getText().toString().trim();
            String desc = binding.etDescription.getText().toString().trim();
            String winnersStr = binding.etWinners.getText().toString().trim();
            final var selectionDeadlineTxt = binding.etSelectionDeadline.getText();
            final var eventDateTxt = binding.etEventDate.getText();
            final var eventTimeTxt = binding.etEventTime.getText();

            if (TextUtils.isEmpty(name)) {
                toast("Please enter an event name.");
                return;
            }
            if (TextUtils.isEmpty(winnersStr)) {
                toast("Please enter number of winners.");
                return;
            }
            if (TextUtils.isEmpty(selectionDeadlineTxt)) {
                toast("Please select a selection date.");
                return;
            }

            if (TextUtils.isEmpty(eventDateTxt)) {
                toast("Please select an event date.");
                return;
            }

            if (TextUtils.isEmpty(eventTimeTxt)) {
                toast("Please select an event time.");
                return;
            }

            btnCreate.setIcon(new LoadingIndicator(requireContext()).getDrawable());
            btnCreate.setText(null);
            btnCreate.setIconPadding(0);
            btnCreate.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);

            final var selectionDeadline = LocalDate.parse(selectionDeadlineTxt, DATE_FORMATTER);
            final var eventDate = LocalDate.parse(eventDateTxt, DATE_FORMATTER);
            final var eventTime = LocalTime.parse(eventTimeTxt, TIME_FORMATTER);

            long winners;
            try {
                winners = Long.parseLong(winnersStr);
            } catch (NumberFormatException e) {
                toast("Winners must be an integer.");
                return;
            }

            Optional<Long> wait = Optional.empty();
            String w = binding.etWaitLimit.getText().toString().trim();
            if (!TextUtils.isEmpty(w)) {
                try {
                    wait = Optional.of(Long.parseLong(w));
                } catch (NumberFormatException e) {
                    toast("Waitlist limit must be an integer.");
                    return;
                }
            }

            final Instant selectionTime =
                    selectionDeadline.atStartOfDay(ZoneId.systemDefault()).toInstant();
            final Instant eventInstant = LocalDateTime.of(eventDate, eventTime)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();

            if (!eventInstant.isAfter(selectionTime)) {
                toast("Event time must be after the selection deadline.");
                return;
            }

            Event created = new Event(
                    name,
                    desc,
                    selectedCategory,
                    binding.enableLocation.isChecked(),
                    new Timestamp(selectionTime),
                    new Timestamp(eventInstant),
                    FirebaseAuthUtils.getCurrentEmail(),
                    winners,
                    wait.orElse(null));

            btnCreate.setEnabled(false);

            EventsDB eventsDB = new EventsDB();

            // Gets the upload promise if an image has been selected through the photo picker.
            var uploadPromise = imageUri != null
                    ? eventsDB.storePoster(created.eventID(), imageUri)
                    : Promise.of(null);

            // Stores the event, and uploads the poster.
            eventsDB.storeEvent(created)
                    .alongside(uploadPromise)
                    .thenRun(_v -> {
                        toast("Event created.");
                        NavHostFragment.findNavController(this).navigateUp();
                    })
                    .catchE(e -> {
                        btnCreate.setEnabled(true);
                        toast("Failed to save event. Try again.");
                    });
        });
    }

    private void setupDatePicker(String title, TextInputEditText target) {
        final var pickerBuilder = MaterialDatePicker.Builder.datePicker();
        pickerBuilder.setTitleText(title);
        // Allow only present and future dates.
        final var calendarConstraints = new CalendarConstraints.Builder();
        calendarConstraints.setValidator(DateValidatorPointForward.now());
        pickerBuilder.setCalendarConstraints(calendarConstraints.build());

        final var targetText = target.getText();
        if (!TextUtils.isEmpty(targetText)) {
            final var existingDate = LocalDate.parse(targetText, DATE_FORMATTER);
            pickerBuilder.setSelection(toEpochMillis(existingDate));
        }

        final var picker = pickerBuilder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                target.setText(DATE_FORMATTER.format(treatAsLocalDate(selection)));
            }
        });

        picker.show(getParentFragmentManager(), "date_picker");
    }

    /**
     * Sets up the event time picker and writes the chosen time to the form
     */
    private void setupEventTimePicker() {
        final View.OnClickListener listener = _v -> {
            final var builder =
                    new MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_12H);

            final var eventTimeText = binding.etEventTime.getText();
            if (!TextUtils.isEmpty(eventTimeText)) {
                final var eventTime = LocalTime.parse(eventTimeText, TIME_FORMATTER);
                builder.setHour(eventTime.getHour());
                builder.setMinute(eventTime.getMinute());
            }

            final var picker = builder.build();
            picker.addOnPositiveButtonClickListener(_selection -> {
                final var eventTime = LocalTime.of(picker.getHour(), picker.getMinute());
                binding.etEventTime.setText(TIME_FORMATTER.format(eventTime));
            });

            picker.show(getParentFragmentManager(), "event_time_picker");
        };

        binding.etEventTime.setOnClickListener(listener);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupCategoryPicker();
    }

    /**
     * Sets up the category spinner.
     */
    private void setupCategoryPicker() {
        final var categorySpinner = binding.spCategory;
        // Set the categories.
        final var categories = Category.values();
        final var labels = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            labels[i] = categories[i].name();
        }
        categorySpinner.setSimpleItems(labels);

        // Set the default.
        categorySpinner.setText(selectedCategory.name(), false);

        categorySpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            selectedCategory = Category.valueOf(selection);
        });
    }

    /**
     * Shows a short-length {@link Toast} with the given message in this Fragment's context
     * @param msg message to display to the user
     */
    private void toast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Clears the view binding reference when the view hierarchy is destroyed to avoid leaks
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
