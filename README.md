# HDS Forms Library (hds-forms-lib)

The **HDS Forms Library** is a specialized Android library designed for dynamic data collection, specifically optimized for HDS-Explorer (Health and Demographic Surveillance Data Explorer) and longitudinal research projects. It provides a robust engine for rendering complex, logic-driven forms as native Android UI components. The library's structure and form design philosophy (HForm) are heavily inspired by **ODK Collect**.

## Key Features

*   **Dynamic UI Rendering**: Automatically generates native Android views based on form definitions using a variety of widgets (Text, Select, Multi-Select, Audio, Image, Video, GPS, Barcode, Date/Time).
*   **Complex Logic Support**: Implements skip logic (`display_condition`), automatic field calculations (`calculation`), and conditional field properties (required, read-only) using the javascript **Apache Commons JEXL** expression engine.
*   **Multimedia & Sensor Integration**:
    *   **Audio Capture**: Record and store audio files directly within the form instance directory.
    *   **Image Capture**: Capture and store photographs within the form instance directory.
    *   **Video Capture**: Record and store video clips within the form instance directory.
    *   **GPS Tracking**: Capture precise location coordinates, altitude, and accuracy.
    *   **Barcode Scanning**: Integrated support for barcode and QR code data entry.
*   **Cascade Selections**: Supports dynamic filtering of options in Select and Multi-Select widgets based on previous answers using `display_condition` at the option level.
*   **Regional Localization & Multi-Calendar Support**: Built-in support for different calendar systems, including the **Ethiopian calendar**, ensuring accuracy in localized demographic studies.
*   **Standardized Data Output**: Serializes collected data into **XML format**, similar to ODK (Open Data Kit) standards and research analysis pipelines.
*   **Multilingual Support**: Ready-to-use translations for English, Portuguese, French, and Amharic.

## Core Components

*   `FormFragment`: The main entry point for hosting and managing the form collection lifecycle.
*   `ColumnView`: The base class for all form widgets, handling rendering, logic evaluation, and state management.
*   `ColumnGroupView`: Manages groups of related columns and handles group-level display logic.
*   `XmlFormResult`: Handles the generation of the final XML result file based on the collected data.

## Usage

The library is typically integrated into a host application by initializing a `FormFragment` with an `HForm` definition or an Excel file (similar to XLSForm file).

```java
FormFragment formFragment = FormFragment.newInstance(
    fragmentManager, 
    hForm, 
    instancesPath, 
    username, 
    preloadedValues, 
    postExecution, 
    backgroundMode, 
    resumeMode, 
    listener
);
```

## HForm Excel Structure

HDS Forms are designed using an Excel spreadsheet (`.xlsx`) and rendered directly into Android widgets. The Excel file must contain three mandatory sheets: `settings`, `options`, and `columns`.

### 1. Settings Sheet
Defines the global properties of the form.

*   **form_id**: The unique, non-spaced identifier for the form.
*   **form_name**: The human-readable name of the form (default language).
*   **form_name::[lang]**: Translations for the form name (e.g., `form_name::pt`, `form_name::fr`, `form_name::am`).
*   **form_version**: The version number of the form definition.
*   **repeat_node_name**: The name of the node used for repeat groups in the generated XML file.

### 2. Options Sheet
Defines the choices available for `select` and `multi_select` question types.

*   **name**: The variable name associated with this set of options.
*   **value**: The actual value stored in the data when this option is selected.
*   **label**: The human-readable text displayed for this option (default language).
*   **label::[lang]**: Translations for the option label.
*   **readonly**: A logic expression (or TRUE/FALSE) determining if the option is locked (useful for pre-filled data).
*   **display_condition**: A logic expression determining if this specific option should be visible to the user. This is used to implement **cascade selections** (e.g., `${province} == '01'`).
*   **display_style**: Specialized formatting for the label (e.g., `selected_only`, `phone_number`).
*   **hidden**: A logic expression determining visibility (prefer using `display_condition`).

### 3. Columns Sheet
Defines the individual questions, notes, and groups within the form.

*   **group**: The variable name for a group. Repeating this name across rows includes multiple questions in the same group.
*   **group_label / group_label::[lang]**: The display name for the group (only required on the first row of the group).
*   **name**: The variable name of the question/field.
*   **type**: The data type (e.g., `text`, `integer`, `decimal`, `select`, `multi_select`, `audio`, `image`, `video`, `gps`, `barcode`, `note`).
*   **options**: For `select` or `multi_select` types, this links to the `name` defined in the **Options** sheet.
*   **repeat_count**: Controls dynamic row generation:
    *   *Empty*: Adds rows dynamically at the end.
    *   *Number*: Fixed number of repetitions.
    *   *Variable*: Evaluates an expression (e.g., `${num_children}`) to get a count.
    *   *Loaded Values*: Iterates over externally loaded data.
*   **label / label::[lang]**: The text of the question displayed to the user.
*   **default_value**: The initial answer when the form starts. Supports JavaScript equations.
*   **calculation**: A JavaScript equation evaluated when the question becomes visible. The result becomes the field's value.
*   **validation**: A logic expression that must evaluate to `true` for the field to be considered valid. If the expression returns `false`, the form blocks navigation and saving.
*   **validation_message / validation_message::[lang]**: The localized error message displayed when the validation expression fails.
*   **required**: A logic expression determining if the field must be answered before finishing.
*   **readonly**: A logic expression determining if the user is prevented from editing the field.
*   **display_condition**: The primary skip logic. If false, the question is invisible and its value is ignored.
*   **display_style**: Specialized formatting (e.g., `selected_only`, `phone_number`).
*   **hidden**: A logic expression. If true, the question is invisible but retains its calculated or default value.

## Logic and Expressions

The library uses **JavaScript-style syntax** for calculations and conditions (`validation`, `required`, `readonly`, `display_condition`, `hidden`).

*   **Variable Referencing**: Access other form values using the `${variable_name}` syntax. You can also reference the **current field's value** using its own name (e.g., in a validation expression for a field named `age`, use `${age} >= 0`).
*   **Operators**: Supports standard JS operators. You can also use friendly aliases like `and` (for `&&`) and `or` (for `||`).
*   **External Method Calls**: You can trigger logic in the host Android application using the `call:` prefix:
    *   *Example*: `call:isInstitutionalHousehold()`
    *   *Implementation*: The host app must implement `FormCollectionListener` and handle the logic within `onFormCallMethod(String methodExpression, String[] args)`.
