package bms.player.beatoraja.modmenu;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

public class ImGuiNotify {

    public static final float NOTIFY_PADDING_X = 20.0f;
    public static final float NOTIFY_PADDING_Y = 20.0f;
    public static final float NOTIFY_PADDING_MESSAGE_Y = 10.0f;
    public static final int NOTIFY_FADE_IN_OUT_TIME = 150;
    public static final int NOTIFY_DEFAULT_DISMISS = 3000;
    public static final float NOTIFY_OPACITY = 0.9f;
    public static final boolean NOTIFY_USE_SEPARATOR = false;
    public static final boolean NOTIFY_USE_DISMISS_BUTTON = false;
    public static final int NOTIFY_RENDER_LIMIT = 7;

    public static final int NOTIFY_DEFAULT_TOAST_FLAGS = ImGuiWindowFlags.AlwaysAutoResize |
            ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoNav |
            ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoFocusOnAppearing;

    public enum ToastType {
        None,
        Success,
        Warning,
        Error,
        Info
    }

    public enum ToastPhase {
        FadeIn,
        Wait,
        FadeOut,
        Expired
    }

    public enum ToastPos {
        TopLeft,
        TopCenter,
        TopRight,
        BottomLeft,
        BottomCenter,
        BottomRight,
        Center
    }

    public static class Toast {
        private ToastType type = ToastType.None;
        private ToastPos pos = ToastPos.TopLeft;
        private String title = "";
        private String content = "";
        private int dismissTime = NOTIFY_DEFAULT_DISMISS;
        private final long creationTime = System.currentTimeMillis();
        private Runnable onButtonPress = null;
        private String buttonLabel = "";

        public Toast(ToastType type) {
            this.type = type;
        }

        public Toast(ToastType type, int dismissTime) {
            this.type = type;
            this.dismissTime = dismissTime;
        }

        public Toast(ToastType type, String content) {
            this.type = type;
            this.content = content;
        }

        public Toast(ToastType type, int dismissTime, String content) {
            this.type = type;
            this.dismissTime = dismissTime;
            this.content = content;
        }

        public Toast(ToastType type, int dismissTime, String buttonLabel, Runnable onButtonPress, String content) {
            this.type = type;
            this.dismissTime = dismissTime;
            this.buttonLabel = buttonLabel;
            this.onButtonPress = onButtonPress;
            this.content = content;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setType(ToastType type) {
            this.type = type;
        }

        public void setPos(ToastPos pos) {
            this.pos = pos;
        }

        public void setOnButtonPress(Runnable onButtonPress) {
            this.onButtonPress = onButtonPress;
        }

        public void setButtonLabel(String buttonLabel) {
            this.buttonLabel = buttonLabel;
        }

        public String getTitle() {
            return title;
        }

        public String getDefaultTitle() {
            if (title == null || title.isEmpty()) {
                return switch (type) {
                    case None -> null;
                    case Success -> "Success";
                    case Warning -> "Warning";
                    case Error -> "Error";
                    case Info -> "Info";
                    default -> null;
                };
            }
            return title;
        }

        public ToastType getType() {
            return type;
        }

        public float[] getColor() {
            return switch (type) {
                case None -> new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // White
                case Success -> new float[]{0.0f, 1.0f, 0.0f, 1.0f}; // Green
                case Warning -> new float[]{1.0f, 1.0f, 0.0f, 1.0f}; // Yellow
                case Error -> new float[]{1.0f, 0.0f, 0.0f, 1.0f}; // Red
                case Info -> new float[]{0.0f, 0.616f, 1.0f, 1.0f}; // Blue
                default -> new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // White
            };
        }

        public String getIcon() {
            return switch (type) {
                case None -> null;
                case Success -> "[OK]";
                case Warning -> "[!]";
                case Error -> "[X]";
                case Info -> "[I]";
                default -> null;
            };
        }

        public String getContent() {
            return content;
        }

        public long getElapsedTime() {
            return System.currentTimeMillis() - creationTime;
        }

        public ToastPhase getPhase() {
            long elapsed = getElapsedTime();
            if (elapsed > NOTIFY_FADE_IN_OUT_TIME + dismissTime + NOTIFY_FADE_IN_OUT_TIME) {
                return ToastPhase.Expired;
            } else if (elapsed > NOTIFY_FADE_IN_OUT_TIME + dismissTime) {
                return ToastPhase.FadeOut;
            } else if (elapsed > NOTIFY_FADE_IN_OUT_TIME) {
                return ToastPhase.Wait;
            } else {
                return ToastPhase.FadeIn;
            }
        }

        public float getFadePercent() {
            ToastPhase phase = getPhase();
            long elapsed = getElapsedTime();

            if (phase == ToastPhase.FadeIn) {
                return ((float) elapsed / NOTIFY_FADE_IN_OUT_TIME) * NOTIFY_OPACITY;
            } else if (phase == ToastPhase.FadeOut) {
                return (1.0f - (((float) elapsed - NOTIFY_FADE_IN_OUT_TIME - dismissTime) / NOTIFY_FADE_IN_OUT_TIME)) * NOTIFY_OPACITY;
            }

            return 1.0f * NOTIFY_OPACITY;
        }

        public int getWindowFlags() {
            return NOTIFY_DEFAULT_TOAST_FLAGS;
        }

        public Runnable getOnButtonPress() {
            return onButtonPress;
        }

        public String getButtonLabel() {
            return buttonLabel;
        }

        public ToastPos getPos() {
            return pos;
        }
    }

    private static final List<Toast> notifications = new ArrayList<>();

    public static void insertNotification(Toast toast) {
        notifications.add(toast);
    }

    public static void removeNotification(int index) {
        if (index >= 0 && index < notifications.size()) {
            notifications.remove(index);
        }
    }

    public static void renderNotifications() {
        float height = 0.0f;

        for (int i = 0; i < notifications.size(); i++) {
            Toast currentToast = notifications.get(i);

            if (currentToast.getPhase() == ToastPhase.Expired) {
                removeNotification(i);
                i--; // This is garbage
                continue;
            }

            if (NOTIFY_RENDER_LIMIT > 0 && i >= NOTIFY_RENDER_LIMIT) {
                continue;
            }

            String icon = currentToast.getIcon();
            String title = currentToast.getTitle();
            String content = currentToast.getContent();
            String defaultTitle = currentToast.getDefaultTitle();
            float opacity = currentToast.getFadePercent();

            float[] textColor = currentToast.getColor();
            textColor[3] = opacity;

            String windowName = "##TOAST" + i;

            ImGui.setNextWindowBgAlpha(opacity);

            // TODO: working in progress
            ToastPos toastPos = currentToast.getPos();
            ImGui.setNextWindowPos(
                    NOTIFY_PADDING_X,
                    NOTIFY_PADDING_Y + height,
                    ImGuiWindowFlags.None
            );

            int windowFlags = currentToast.getWindowFlags();
            if (!NOTIFY_USE_DISMISS_BUTTON && currentToast.getOnButtonPress() == null) {
                windowFlags |= ImGuiWindowFlags.NoInputs;
            }

            ImGui.begin(windowName, windowFlags);

            ImGui.pushTextWrapPos(windowWidth / 3.0f);

            boolean wasTitleRendered = false;

            if (icon != null && !icon.isEmpty()) {
                ImGui.textColored(textColor[0], textColor[1], textColor[2], textColor[3], icon);
                wasTitleRendered = true;
            }

            if (title != null && !title.isEmpty()) {
                if (wasTitleRendered) {
                    ImGui.sameLine();
                }
                ImGui.text(title);
                wasTitleRendered = true;
            } else if (defaultTitle != null && !defaultTitle.isEmpty()) {
                if (wasTitleRendered) {
                    ImGui.sameLine();
                }
                ImGui.text(defaultTitle);
                wasTitleRendered = true;
            }

            if (NOTIFY_USE_DISMISS_BUTTON) {
                if (wasTitleRendered || (content != null && !content.isEmpty())) {
                    ImGui.sameLine();
                }

                if (ImGui.button("X")) {
                    removeNotification(i);
                    ImGui.end();
                    break;
                }
            }

            if (wasTitleRendered && content != null && !content.isEmpty()) {
                ImGui.setCursorPosY(ImGui.getCursorPosY() + 5.0f);
            }

            if (content != null && !content.isEmpty()) {
                if (wasTitleRendered && NOTIFY_USE_SEPARATOR) {
                    ImGui.separator();
                }
                ImGui.text(content);
            }

            if (currentToast.getOnButtonPress() != null) {
                if (ImGui.button(currentToast.getButtonLabel())) {
                    currentToast.getOnButtonPress().run();
                }
            }

            ImGui.popTextWrapPos();

            height += ImGui.getWindowHeight() + NOTIFY_PADDING_MESSAGE_Y;

            ImGui.end();
        }
    }

    private static Pair<Float, Float> getRelativeInitPos(ToastPos posType) {
        return switch (posType) {
            case Center -> new Pair<>(windowWidth * 0.5f, windowHeight * 0.5f);
            case TopLeft -> new Pair<>(NOTIFY_PADDING_X, NOTIFY_PADDING_Y);
            case TopCenter -> new Pair<>(windowWidth * 0.5f, NOTIFY_PADDING_Y);
            case TopRight -> new Pair<>(windowWidth - NOTIFY_PADDING_X, NOTIFY_PADDING_Y);
            case BottomLeft -> new Pair<>(NOTIFY_PADDING_X, windowHeight - NOTIFY_PADDING_Y);
            case BottomCenter -> new Pair<>(windowWidth * 0.5f, windowHeight - NOTIFY_PADDING_Y);
            case BottomRight -> new Pair<>(windowWidth - NOTIFY_PADDING_X, windowHeight - NOTIFY_PADDING_Y);
        };
    }

    // Notifications
    public static void success(String content) {
        insertNotification(new Toast(ToastType.Success, content));
    }

    public static void success(String content, int dismissTime) {
        insertNotification(new Toast(ToastType.Success, dismissTime, content));
    }

    public static void warning(String content) {
        insertNotification(new Toast(ToastType.Warning, content));
    }

    public static void warning(String content, int dismissTime) {
        insertNotification(new Toast(ToastType.Warning, dismissTime, content));
    }

    public static void error(String content) {
        insertNotification(new Toast(ToastType.Error, content));
    }

    public static void error(String content, int dismissTime) {
        insertNotification(new Toast(ToastType.Error, dismissTime, content));
    }

    public static void info(String content) {
        insertNotification(new Toast(ToastType.Info, content));
    }

    public static void info(String content, int dismissTime) {
        insertNotification(new Toast(ToastType.Info, dismissTime, content));
    }

    public static void withButton(ToastType type, String content, String buttonLabel, Runnable onButtonPress) {
        insertNotification(new Toast(type, NOTIFY_DEFAULT_DISMISS, buttonLabel, onButtonPress, content));
    }
}