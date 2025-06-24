# Shared Components and Styles

This directory contains shared components and styles that can be used across different parts of the application.

## Directory Structure

```
shared/
├── components/
│   └── sidebar/
│       └── Sidebar.fxml       # Reusable sidebar component
└── styles/
    ├── base.css              # Base styles and variables
    └── sidebar.css           # Sidebar-specific styles
```

## How to Use

### 1. Including Shared Styles

In your FXML file, add the following stylesheets:

```xml
<stylesheets>
    <URL value="@../shared/styles/base.css"/>
    <URL value="@../shared/styles/sidebar.css"/>
</stylesheets>
```

### 2. Using the Sidebar Component

1. Include the sidebar in your FXML:
```xml
<fx:include source="../shared/components/sidebar/Sidebar.fxml" />
```

2. In your controller, inject the sidebar:
```java
@FXML
private VBox sidebar;
```

3. Initialize the sidebar buttons in your controller's initialize method:
```java
@FXML
private void initialize() {
    // Get sidebar controller
    SidebarController sidebarController = sidebarLoader.getController();
    
    // Set up event handlers
    sidebarController.setOnDashboardAction(event -> handleDashboard());
    sidebarController.setOnProfileAction(event -> handleProfile());
    // ... set up other handlers
}
```

## Available Components

### Sidebar
- Includes user avatar
- Navigation menu with icons
- Logout button
- Customizable through CSS classes

## Style Classes

### Base Styles (`base.css`)
- Color variables
- Common form styles
- Typography
- Basic animations

### Sidebar Styles (`sidebar.css`)
- `.sidebar` - Main sidebar container
- `.sidebar-title` - Sidebar header
- `.sidebar-avatar` - User avatar
- `.sidebar-button` - Navigation buttons
- `.sidebar-button.selected` - Selected state for buttons

## Best Practices

1. Always use the provided color variables from `base.css`
2. Maintain consistent spacing using the predefined padding and margins
3. Follow the existing naming conventions for CSS classes
4. Use the shared components to ensure UI consistency
