package com.evilgrandpa.marketplace;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(16, 9, 13);
    private static final int PANEL = Color.rgb(34, 19, 26);
    private static final int PANEL_LIGHT = Color.rgb(48, 25, 34);
    private static final int RED = Color.rgb(184, 31, 57);
    private static final int RED_DARK = Color.rgb(104, 20, 38);
    private static final int RED_BRIGHT = Color.rgb(241, 74, 98);
    private static final int FG = Color.rgb(250, 240, 243);
    private static final int MUTED = Color.rgb(195, 164, 174);
    private static final int GOLD = Color.rgb(255, 204, 116);
    private static final String[] CATEGORIES = {"All", "Garden", "Electronics", "Collectibles", "Misc"};
    private static final String[] REPLIES = {
            "NO LOWBALLERS. I KNOW WHAT I GOT.",
            "BACK IN MY DAY THIS COST A NICKEL.",
            "IS THIS STILL AVAILABLE? WAIT, I AM THE SELLER.",
            "CASH ONLY. IMAGINARY CASH, OBVIOUSLY.",
            "MY GRANDSON SAID I SHOULD BLOCK YOU.",
            "COME GET IT BEFORE MY NEIGHBOR DOES.",
            "WHY ARE YOU WHISPERING? TYPE LOUDER."
    };

    private final Random random = new Random();
    private final ArrayList<Listing> listings = new ArrayList<>();
    private final LinkedHashMap<String, ArrayList<Message>> chats = new LinkedHashMap<>();
    private final ArrayList<Review> reviews = new ArrayList<>();
    private final ArrayList<OfferRecord> offers = new ArrayList<>();
    private final ArrayList<Receipt> purchases = new ArrayList<>();
    private final LinkedHashSet<String> reportedListings = new LinkedHashSet<>();
    private final LinkedHashSet<String> reportedScamCards = new LinkedHashSet<>();

    private LinearLayout appRoot;
    private FrameLayout pageHost;
    private TextView statusBar;
    private LinearLayout marketResults;
    private String currentPage = "market";
    private String currentSeller = "Grandpa Earl";
    private String selectedCategory = "All";
    private String marketQuery = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(RED_DARK);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        loadState();
        buildShell();
        showPage("market");
    }

    @Override
    protected void onStop() {
        saveState();
        super.onStop();
    }

    private void buildShell() {
        appRoot = new LinearLayout(this);
        appRoot.setOrientation(LinearLayout.VERTICAL);
        appRoot.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(18), dp(12), dp(18), dp(12));
        header.setBackgroundColor(RED_DARK);
        TextView brand = text("☠  EVIL GRANDPA", 21, Color.WHITE, true);
        TextView subtitle = text("MARKETPLACE  ·  LOCAL · FICTIONAL · QUESTIONABLE", 10, Color.rgb(255, 193, 204), true);
        header.addView(brand);
        header.addView(subtitle);
        appRoot.addView(header, new LinearLayout.LayoutParams(-1, -2));

        pageHost = new FrameLayout(this);
        appRoot.addView(pageHost, new LinearLayout.LayoutParams(-1, 0, 1));

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        navScroll.setBackgroundColor(PANEL);
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(8), dp(7), dp(8), dp(7));
        String[][] tabs = {
                {"market", "🏚 Market"}, {"chat", "✉ Chat"}, {"sell", "+ Sell"},
                {"reviews", "★ Reviews"}, {"scams", "⚠ Scam lab"}, {"receipts", "🛒 Receipts"}
        };
        for (String[] tab : tabs) {
            TextView button = action(tab[1], RED_DARK, () -> showPage(tab[0]));
            button.setTextSize(11);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(42));
            lp.setMargins(dp(4), 0, dp(4), 0);
            nav.addView(button, lp);
        }
        navScroll.addView(nav);
        appRoot.addView(navScroll, new LinearLayout.LayoutParams(-1, dp(56)));

        statusBar = text("WELCOME TO THE WORST YARD SALE ON EARTH", 10, Color.rgb(255, 190, 202), false);
        statusBar.setPadding(dp(15), dp(9), dp(15), dp(9));
        statusBar.setBackgroundColor(Color.rgb(39, 14, 23));
        appRoot.addView(statusBar, new LinearLayout.LayoutParams(-1, -2));
        setContentView(appRoot);
    }

    private void showPage(String page) {
        currentPage = page;
        pageHost.removeAllViews();
        View content;
        switch (page) {
            case "chat": content = chatScreen(); break;
            case "sell": content = sellScreen(); break;
            case "reviews": content = reviewsScreen(); break;
            case "scams": content = scamsScreen(); break;
            case "receipts": content = receiptsScreen(); break;
            default: currentPage = "market"; content = marketScreen(); break;
        }
        pageHost.addView(content, new FrameLayout.LayoutParams(-1, -1));
    }

    private View marketScreen() {
        LinearLayout page = vertical();
        page.setPadding(dp(16), dp(16), dp(16), dp(10));
        page.addView(text("THE MARKETPLACE", 21, FG, true));
        page.addView(text("Absolutely terrible deals from suspiciously confident seniors.", 11, MUTED, false), topMargin(2));

        EditText search = edit("Search cursed listings or sellers", InputType.TYPE_CLASS_TEXT);
        search.setSingleLine(true);
        LinearLayout.LayoutParams searchLp = topMargin(14);
        page.addView(search, searchLp);

        HorizontalScrollView categories = new HorizontalScrollView(this);
        categories.setHorizontalScrollBarEnabled(false);
        LinearLayout categoryRow = new LinearLayout(this);
        categoryRow.setPadding(0, dp(9), 0, dp(9));
        for (String category : CATEGORIES) {
            TextView chip = action(category, category.equals(selectedCategory) ? RED : PANEL_LIGHT,
                    () -> { selectedCategory = category; showPage("market"); });
            chip.setTextSize(10);
            LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(-2, dp(36));
            chipLp.setMargins(0, 0, dp(7), 0);
            categoryRow.addView(chip, chipLp);
        }
        categories.addView(categoryRow);
        page.addView(categories, new LinearLayout.LayoutParams(-1, -2));

        marketResults = vertical();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(marketResults);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        renderMarketResults();
        search.setText(marketQuery);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                marketQuery = s.toString();
                renderMarketResults();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        return page;
    }

    private void renderMarketResults() {
        if (marketResults == null) return;
        marketResults.removeAllViews();
        String q = marketQuery.trim().toLowerCase(Locale.ROOT);
        int found = 0;
        for (Listing listing : listings) {
            String searchable = (listing.name + " " + listing.seller + " " + listing.description + " " + listing.category).toLowerCase(Locale.ROOT);
            if (!searchable.contains(q)) continue;
            if (!"All".equals(selectedCategory) && !selectedCategory.equals(listing.category)) continue;
            marketResults.addView(listingCard(listing), bottomMargin(9));
            found++;
        }
        if (found == 0) {
            TextView empty = text("NO JUNK FOUND. THE GRANDPAS ARE DISAPPOINTED.", 12, MUTED, true);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20), dp(32), dp(20), dp(32));
            marketResults.addView(empty);
        }
    }

    private View listingCard(Listing listing) {
        LinearLayout card = card();
        LinearLayout top = horizontal();
        TextView name = text(listing.name, 14, FG, true);
        name.setMaxLines(2);
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(text("$" + listing.price, 18, GOLD, true));
        card.addView(top);
        card.addView(text(listing.category + "  ·  " + listing.seller + "  ·  " + stars(listing.rating), 10, MUTED, true), topMargin(6));
        TextView description = text(listing.description, 11, Color.rgb(227, 202, 210), false);
        description.setMaxLines(3);
        card.addView(description, topMargin(7));
        LinearLayout actions = horizontal();
        actions.addView(action("DETAILS", RED, () -> showListing(listing)));
        actions.addView(action("CHAT", PANEL_LIGHT, () -> openChat(listing.seller)), leftMargin(6));
        actions.addView(action("FLAG", PANEL_LIGHT, () -> reportListing(listing)), leftMargin(6));
        card.addView(actions, topMargin(11));
        return card;
    }

    private void showListing(Listing listing) {
        LinearLayout panel = vertical();
        panel.setPadding(dp(20), dp(20), dp(20), dp(18));
        panel.setBackground(rounded(PANEL, 20, PANEL_LIGHT));
        panel.addView(text("CURSED LISTING", 10, RED_BRIGHT, true));
        panel.addView(text(listing.name, 19, FG, true), topMargin(8));
        panel.addView(text("$" + listing.price + "  ·  " + listing.category + "  ·  " + stars(listing.rating), 14, GOLD, true), topMargin(8));
        panel.addView(text("Sold by " + listing.seller, 11, MUTED, false), topMargin(5));
        panel.addView(text(listing.description, 13, FG, false), topMargin(12));
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        panel.addView(action("BUY THIS JUNK  ·  NO REAL MONEY", RED, () -> confirmBuy(listing, dialog)), topMargin(15));
        panel.addView(action("MESSAGE GRANDPA", PANEL_LIGHT, () -> { dialog.dismiss(); openChat(listing.seller); }), topMargin(8));
        panel.addView(action("MAKE A TERRIBLE OFFER", PANEL_LIGHT, () -> makeOffer(listing, dialog)), topMargin(8));
        if ("YOU".equals(listing.seller)) {
            panel.addView(action("DELETE MY LISTING", RED_DARK, () -> deleteListing(listing, dialog)), topMargin(8));
        }
        panel.addView(action("REPORT AS SUSPICIOUS", PANEL_LIGHT, () -> { dialog.dismiss(); reportListing(listing); }), topMargin(8));
        dialog.setContentView(panel);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setLayout(getResources().getDisplayMetrics().widthPixels - dp(28), -2);
        }
        dialog.show();
        if (dialog.getWindow() != null) dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels - dp(28), -2);
    }

    private void confirmBuy(Listing listing, Dialog parent) {
        new AlertDialog.Builder(this)
                .setTitle("Imaginary purchase")
                .setMessage("Buy " + listing.name + " for $" + listing.price + "?\n\nThis is fictional. No payment is collected and no real money moves.")
                .setNegativeButton("CANCEL", (d, which) -> { })
                .setPositiveButton("COMPLETE FAKE PURCHASE", (d, which) -> {
                    purchases.add(new Receipt(listing.name, listing.seller, listing.price, System.currentTimeMillis()));
                    saveState();
                    setStatus("PURCHASE SIMULATED. GRANDPA IS COUNTING IMAGINARY CASH.");
                    Toast.makeText(this, "Fake receipt saved — no payment happened.", Toast.LENGTH_LONG).show();
                    parent.dismiss();
                }).show();
    }

    private void makeOffer(Listing listing, Dialog parent) {
        EditText amount = edit("Offer amount in whole dollars", InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle("Lowball offer")
                .setMessage("Asking price: $" + listing.price + " · This is only a local joke.")
                .setView(amount)
                .setNegativeButton("CANCEL", (d, which) -> { })
                .setPositiveButton("SEND OFFER", (d, which) -> {
                    Integer value = parsePrice(amount.getText().toString());
                    if (value == null) {
                        Toast.makeText(this, "Enter a valid whole-dollar offer.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean accepted = value >= listing.price;
                    String reply = accepted ? "FINE. DEAL. BUT I AM STILL ANGRY." : REPLIES[random.nextInt(REPLIES.length)];
                    offers.add(new OfferRecord(listing.name, listing.seller, listing.price, value, reply));
                    saveState();
                    setStatus("YOUR OFFER HAS BEEN JUDGED BY THE COUNCIL OF GRANDPAS.");
                    new AlertDialog.Builder(this).setTitle("Grandpa responds").setMessage(reply + "\n\nNo real transaction occurred.").setPositiveButton("OK", null).show();
                    parent.dismiss();
                }).show();
    }

    private void deleteListing(Listing listing, Dialog parent) {
        new AlertDialog.Builder(this)
                .setTitle("Delete your listing?")
                .setMessage("Remove “" + listing.name + "” from your local marketplace?")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("DELETE", (d, which) -> {
                    listings.remove(listing);
                    reportedListings.remove(listing.id);
                    saveState();
                    parent.dismiss();
                    setStatus("YOUR JUNK HAS BEEN REMOVED.");
                    showPage("market");
                }).show();
    }

    private void reportListing(Listing listing) {
        if (reportedListings.add(listing.id)) {
            saveState();
            setStatus("LISTING FLAGGED LOCALLY. THE SCAM RADAR HAS BEEN NOTIFIED.");
            Toast.makeText(this, "Flag saved to Scam Lab.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "This listing is already flagged.", Toast.LENGTH_SHORT).show();
        }
    }

    private View chatScreen() {
        LinearLayout page = vertical();
        page.setPadding(dp(16), dp(16), dp(16), dp(10));
        page.addView(text("GRANDPA MESSENGER", 21, FG, true));
        page.addView(text("The caps-lock key is stuck. Every chat stays on this device.", 11, MUTED, false), topMargin(3));

        HorizontalScrollView sellerScroll = new HorizontalScrollView(this);
        sellerScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout sellerRow = new LinearLayout(this);
        sellerRow.setPadding(0, dp(12), 0, dp(10));
        for (String seller : sellerNames()) {
            TextView chip = action(seller, seller.equals(currentSeller) ? RED : PANEL_LIGHT, () -> {
                currentSeller = seller;
                showPage("chat");
            });
            chip.setTextSize(10);
            LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(-2, dp(38));
            chipLp.setMargins(0, 0, dp(7), 0);
            sellerRow.addView(chip, chipLp);
        }
        sellerScroll.addView(sellerRow);
        page.addView(sellerScroll, new LinearLayout.LayoutParams(-1, -2));

        ScrollView transcript = new ScrollView(this);
        transcript.setFillViewport(true);
        LinearLayout messages = vertical();
        messages.setPadding(dp(12), dp(12), dp(12), dp(12));
        messages.setBackground(rounded(PANEL, 16, PANEL_LIGHT));
        ArrayList<Message> history = chats.get(currentSeller);
        if (history == null || history.isEmpty()) {
            messages.addView(messageBubble(currentSeller, "WHAT DO YOU WANT, YOUNGSTER?", false), bottomMargin(9));
        } else {
            for (Message message : history) {
                messages.addView(messageBubble(message.sender, message.body, "YOU".equals(message.sender)), bottomMargin(9));
            }
        }
        transcript.addView(messages);
        page.addView(transcript, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout composer = horizontal();
        EditText input = edit("Type a message...", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setSingleLine(true);
        composer.addView(input, new LinearLayout.LayoutParams(0, dp(48), 1));
        composer.addView(action("SEND", RED, () -> sendMessage(input.getText().toString())), leftMargin(8));
        page.addView(composer, topMargin(9));
        transcript.post(() -> transcript.fullScroll(View.FOCUS_DOWN));
        return page;
    }

    private View messageBubble(String sender, String body, boolean mine) {
        LinearLayout row = horizontal();
        row.setGravity(mine ? Gravity.RIGHT : Gravity.LEFT);
        LinearLayout bubble = vertical();
        bubble.setPadding(dp(11), dp(9), dp(11), dp(9));
        bubble.setBackground(rounded(mine ? RED_DARK : PANEL_LIGHT, 14, mine ? RED : PANEL_LIGHT));
        bubble.addView(text(mine ? "YOU" : sender.toUpperCase(Locale.ROOT), 9, mine ? Color.rgb(255, 200, 208) : GOLD, true));
        bubble.addView(text(body, 12, FG, false), topMargin(3));
        row.addView(bubble, new LinearLayout.LayoutParams(-2, -2));
        return row;
    }

    private void sendMessage(String raw) {
        String body = raw == null ? "" : raw.trim();
        if (body.isEmpty()) return;
        ArrayList<Message> history = chats.get(currentSeller);
        if (history == null) {
            history = new ArrayList<>();
            chats.put(currentSeller, history);
        }
        history.add(new Message("YOU", body.substring(0, Math.min(500, body.length()))));
        history.add(new Message(currentSeller, REPLIES[random.nextInt(REPLIES.length)]));
        saveState();
        setStatus("MESSAGE SENT TO " + currentSeller.toUpperCase(Locale.ROOT) + ". THEIR CAPS LOCK IS STILL BROKEN.");
        showPage("chat");
    }

    private void openChat(String seller) {
        if (seller == null || seller.trim().isEmpty() || "YOU".equals(seller)) seller = "Grandpa Earl";
        currentSeller = seller;
        showPage("chat");
    }

    private View sellScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = vertical();
        page.setPadding(dp(16), dp(16), dp(16), dp(22));
        page.addView(text("SELL YOUR JUNK", 21, FG, true));
        page.addView(text("Post a fictional listing. Keep your imaginary empire growing.", 11, MUTED, false), topMargin(3));

        EditText name = edit("Item name", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        EditText price = edit("Price in whole dollars", InputType.TYPE_CLASS_NUMBER);
        EditText description = edit("Description", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        description.setGravity(Gravity.TOP | Gravity.START);
        description.setMinLines(4);
        addField(page, "ITEM NAME", name, 16);
        addField(page, "ASKING PRICE", price, 13);
        addField(page, "DESCRIPTION", description, 13);
        page.addView(text("CATEGORY", 10, MUTED, true), topMargin(15));
        final String[] category = {"Misc"};
        LinearLayout categoryHolder = vertical();
        page.addView(categoryHolder, topMargin(8));
        renderSellCategories(categoryHolder, category);

        page.addView(action("POST LISTING", RED, () -> {
            String itemName = name.getText().toString().trim();
            String itemDescription = description.getText().toString().trim();
            Integer amount = parsePrice(price.getText().toString());
            if (itemName.isEmpty() || itemDescription.isEmpty() || amount == null) {
                Toast.makeText(this, "Add a name, valid whole-dollar price, and description.", Toast.LENGTH_LONG).show();
                return;
            }
            int nextId = 1;
            for (Listing item : listings) nextId = Math.max(nextId, item.numericId() + 1);
            listings.add(0, new Listing(String.valueOf(nextId), itemName.substring(0, Math.min(80, itemName.length())), amount,
                    "YOU", category[0], itemDescription.substring(0, Math.min(500, itemDescription.length())), 5));
            saveState();
            setStatus("YOUR JUNK IS NOW LISTED.");
            Toast.makeText(this, "Listing posted locally.", Toast.LENGTH_SHORT).show();
            selectedCategory = "All";
            marketQuery = "";
            showPage("market");
        }), topMargin(18));
        scroll.addView(page);
        return scroll;
    }

    private void renderSellCategories(LinearLayout holder, String[] selected) {
        holder.removeAllViews();
        LinearLayout row = horizontal();
        for (String category : Arrays.copyOfRange(CATEGORIES, 1, CATEGORIES.length)) {
            TextView chip = action(category, category.equals(selected[0]) ? RED : PANEL_LIGHT, () -> {
                selected[0] = category;
                renderSellCategories(holder, selected);
            });
            chip.setTextSize(10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(36));
            lp.setMargins(0, 0, dp(7), 0);
            row.addView(chip, lp);
        }
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        scroller.addView(row);
        holder.addView(scroller);
    }

    private View reviewsScreen() {
        LinearLayout page = vertical();
        page.setPadding(dp(16), dp(16), dp(16), dp(12));
        page.addView(text("GRANDPA REVIEWS", 21, FG, true));
        page.addView(text("The customers are mad. The sellers are madder.", 11, MUTED, false), topMargin(3));
        LinearLayout list = vertical();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(list);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        for (String seller : sellerNames()) {
            LinearLayout box = card();
            box.addView(text(seller, 15, FG, true));
            ArrayList<Review> sellerReviews = reviewsFor(seller);
            if (sellerReviews.isEmpty()) {
                List<String> defaults = defaultReviews(seller);
                for (String line : defaults) box.addView(text(line, 10, MUTED, false), topMargin(7));
            } else {
                for (Review review : sellerReviews) {
                    box.addView(text(stars(review.rating) + "  " + review.body, 10, MUTED, false), topMargin(7));
                }
            }
            box.addView(action("WRITE A REVIEW", PANEL_LIGHT, () -> writeReview(seller)), topMargin(10));
            list.addView(box, bottomMargin(9));
        }
        return page;
    }

    private void writeReview(String seller) {
        LinearLayout form = vertical();
        form.setPadding(dp(2), dp(8), dp(2), 0);
        RatingBar rating = new RatingBar(this);
        rating.setNumStars(5);
        rating.setStepSize(1f);
        rating.setRating(5f);
        form.addView(rating);
        EditText body = edit("What went wrong (or right)?", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        body.setMinLines(3);
        form.addView(body, topMargin(10));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Review " + seller)
                .setView(form)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE REVIEW", null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String note = body.getText().toString().trim();
            if (note.isEmpty()) {
                Toast.makeText(this, "Write a short review first.", Toast.LENGTH_SHORT).show();
                return;
            }
            reviews.add(new Review(seller, Math.max(1, Math.min(5, Math.round(rating.getRating()))), note.substring(0, Math.min(300, note.length()))));
            saveState();
            dialog.dismiss();
            showPage("reviews");
        }));
        dialog.show();
    }

    private View scamsScreen() {
        LinearLayout page = vertical();
        page.setPadding(dp(16), dp(16), dp(16), dp(12));
        page.addView(text("SCAM LAB", 21, FG, true));
        page.addView(text("Fake scam warnings and local reports. Nothing here connects to real payments.", 11, MUTED, false), topMargin(3));

        ScrollView scroll = new ScrollView(this);
        LinearLayout content = vertical();
        scroll.addView(content);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout warning = card();
        warning.setBackground(rounded(Color.rgb(55, 22, 30), 16, RED_DARK));
        warning.addView(text("☣  SAFETY CHECK", 13, RED_BRIGHT, true));
        warning.addView(text("These are comedy examples. Never send gift cards, passwords, or real money to strangers.", 11, FG, false), topMargin(7));
        content.addView(warning, bottomMargin(12));

        content.addView(text("SCAM SPOTTER", 12, GOLD, true), bottomMargin(7));
        String[][] scams = {
                {"THE GRANDSON EMERGENCY", "“My grandson is trapped in a videogame. Pay me in gift cards.”"},
                {"INVISIBLE FENCE DEPOSIT", "Seller wants a deposit before showing an item that cannot be seen."},
                {"MYSTERY LINK", "“Click this totally normal link to claim your haunted toaster.”"}
        };
        for (String[] scam : scams) {
            LinearLayout box = card();
            box.addView(text(scam[0], 13, FG, true));
            box.addView(text(scam[1], 11, MUTED, false), topMargin(6));
            boolean reported = reportedScamCards.contains(scam[0]);
            box.addView(action(reported ? "MARKED AS A JOKE SCAM" : "REPORT THIS SCAM EXAMPLE", reported ? RED_DARK : PANEL_LIGHT,
                    () -> { reportedScamCards.add(scam[0]); saveState(); setStatus("SCAM EXAMPLE FLAGGED LOCALLY."); showPage("scams"); }), topMargin(9));
            content.addView(box, bottomMargin(8));
        }

        content.addView(text("FLAGGED LISTINGS  ·  " + reportedListings.size(), 12, GOLD, true), topMargin(6));
        if (reportedListings.isEmpty()) {
            content.addView(text("No listings flagged yet. Use FLAG on a market card to report one here.", 10, MUTED, false), topMargin(5));
        } else {
            for (String id : reportedListings) {
                Listing item = findListing(id);
                if (item == null) continue;
                LinearLayout box = card();
                box.addView(text(item.name, 12, FG, true));
                box.addView(text(item.seller + "  ·  $" + item.price + "  ·  local report", 10, MUTED, false), topMargin(5));
                content.addView(box, topMargin(7));
            }
        }

        content.addView(text("LOWBALL OFFER HISTORY  ·  " + offers.size(), 12, GOLD, true), topMargin(15));
        if (offers.isEmpty()) {
            content.addView(text("No offers yet. Open a listing and make Grandpa mad.", 10, MUTED, false), topMargin(5));
        } else {
            for (int i = offers.size() - 1; i >= 0; i--) {
                OfferRecord offer = offers.get(i);
                LinearLayout box = card();
                box.addView(text(offer.item + "  ·  asked $" + offer.asking + " / offered $" + offer.offered, 11, FG, true));
                box.addView(text(offer.seller + ": " + offer.reply, 10, GOLD, false), topMargin(6));
                content.addView(box, topMargin(7));
            }
        }
        return page;
    }

    private View receiptsScreen() {
        LinearLayout page = vertical();
        page.setPadding(dp(16), dp(16), dp(16), dp(12));
        page.addView(text("MY PURCHASES", 21, FG, true));
        page.addView(text("Fake receipts for fake transactions. No real money involved.", 11, MUTED, false), topMargin(3));
        LinearLayout list = vertical();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(list);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        if (purchases.isEmpty()) {
            TextView empty = text("YOU HAVE NOT PURCHASED ANY CURSED OBJECTS YET.", 12, MUTED, true);
            empty.setPadding(dp(10), dp(22), dp(10), dp(22));
            list.addView(empty);
        }
        for (int i = purchases.size() - 1; i >= 0; i--) {
            Receipt receipt = purchases.get(i);
            LinearLayout box = card();
            box.addView(text(receipt.name + "  ·  $" + receipt.price, 14, FG, true));
            box.addView(text("SOLD BY " + receipt.seller.toUpperCase(Locale.ROOT) + "  ·  SIMULATED PURCHASE", 10, MUTED, false), topMargin(5));
            contentTime(box, receipt.time);
            list.addView(box, bottomMargin(8));
        }
        return page;
    }

    private void contentTime(LinearLayout box, long millis) {
        if (millis <= 0) return;
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());
        box.addView(text(format.format(new java.util.Date(millis)), 9, MUTED, false), topMargin(4));
    }

    private void addField(LinearLayout page, String label, EditText field, int top) {
        page.addView(text(label, 10, MUTED, true), topMargin(top));
        page.addView(field, topMargin(5));
    }

    private void loadState() {
        SharedPreferences prefs = getSharedPreferences("evil_grandpa_marketplace", Context.MODE_PRIVATE);
        String raw = prefs.getString("state", null);
        if (raw == null || raw.trim().isEmpty()) {
            seedListings();
            return;
        }
        try {
            JSONObject root = new JSONObject(raw);
            JSONArray itemArray = root.optJSONArray("items");
            if (itemArray != null) {
                for (int i = 0; i < itemArray.length(); i++) listings.add(new Listing(itemArray.getJSONObject(i)));
            }
            JSONObject chatObject = root.optJSONObject("messages");
            if (chatObject != null) {
                java.util.Iterator<String> keys = chatObject.keys();
                while (keys.hasNext()) {
                    String seller = keys.next();
                    JSONArray history = chatObject.optJSONArray(seller);
                    ArrayList<Message> messages = new ArrayList<>();
                    if (history != null) for (int i = 0; i < history.length(); i++) messages.add(new Message(history.getJSONObject(i)));
                    chats.put(seller, messages);
                }
            }
            JSONArray reviewArray = root.optJSONArray("reviews");
            if (reviewArray != null) for (int i = 0; i < reviewArray.length(); i++) reviews.add(new Review(reviewArray.getJSONObject(i)));
            JSONArray offerArray = root.optJSONArray("offers");
            if (offerArray != null) for (int i = 0; i < offerArray.length(); i++) offers.add(new OfferRecord(offerArray.getJSONObject(i)));
            JSONArray purchaseArray = root.optJSONArray("purchases");
            if (purchaseArray != null) for (int i = 0; i < purchaseArray.length(); i++) purchases.add(new Receipt(purchaseArray.getJSONObject(i)));
            readStrings(root.optJSONArray("reportedListings"), reportedListings);
            readStrings(root.optJSONArray("reportedScamCards"), reportedScamCards);
            if (listings.isEmpty()) seedListings();
        } catch (JSONException exception) {
            listings.clear();
            chats.clear();
            reviews.clear();
            offers.clear();
            purchases.clear();
            reportedListings.clear();
            reportedScamCards.clear();
            seedListings();
        }
    }

    private void readStrings(JSONArray array, Set<String> target) {
        if (array == null) return;
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "");
            if (!value.isEmpty()) target.add(value);
        }
    }

    private void seedListings() {
        listings.clear();
        listings.add(new Listing("1", "HAUNTED LAWNMOWER", 45, "Grandpa Earl", "Garden", "Starts only at 3 AM. Yells at squirrels. NO REFUNDS.", 2));
        listings.add(new Listing("2", "1998 TV (MOSTLY WORKS)", 70, "Old Man Jenkins", "Electronics", "Screen is green. Remote is a brick. PICKUP ONLY.", 1));
        listings.add(new Listing("3", "CURSED DENTURES", 12, "Grandpa Walter", "Collectibles", "They chatter on their own. Slightly used.", 3));
        listings.add(new Listing("4", "MYSTERY GARAGE BOX", 9, "Gramps McGee", "Misc", "I FORGOT WHAT IS IN IT. Do not shake.", 1));
        listings.add(new Listing("5", "VERY ANGRY GARDEN GNOME", 150, "Grandpa Earl", "Garden", "Stares at the neighbors. Knows your secrets.", 4));
        listings.add(new Listing("6", "VINTAGE COMPUTER MOUSE", 25, "Old Man Jenkins", "Electronics", "Ball mouse. Comes with 17 years of dust.", 2));
        listings.add(new Listing("7", "ONE (1) SPOON", 400, "Grandpa Walter", "Collectibles", "RARE. MY GRANDSON SAYS IT IS NOT. HE IS WRONG.", 5));
        listings.add(new Listing("8", "INVISIBLE FENCE", 99, "Gramps McGee", "Garden", "Cannot show photos because it is INVISIBLE.", 1));
    }

    private void saveState() {
        JSONObject root = new JSONObject();
        try {
            JSONArray itemArray = new JSONArray();
            for (Listing item : listings) itemArray.put(item.toJson());
            root.put("items", itemArray);
            JSONObject chatObject = new JSONObject();
            for (Map.Entry<String, ArrayList<Message>> entry : chats.entrySet()) {
                JSONArray history = new JSONArray();
                for (Message message : entry.getValue()) history.put(message.toJson());
                chatObject.put(entry.getKey(), history);
            }
            root.put("messages", chatObject);
            JSONArray reviewArray = new JSONArray();
            for (Review review : reviews) reviewArray.put(review.toJson());
            root.put("reviews", reviewArray);
            JSONArray offerArray = new JSONArray();
            for (OfferRecord offer : offers) offerArray.put(offer.toJson());
            root.put("offers", offerArray);
            JSONArray purchaseArray = new JSONArray();
            for (Receipt receipt : purchases) purchaseArray.put(receipt.toJson());
            root.put("purchases", purchaseArray);
            root.put("reportedListings", stringsToJson(reportedListings));
            root.put("reportedScamCards", stringsToJson(reportedScamCards));
            getSharedPreferences("evil_grandpa_marketplace", Context.MODE_PRIVATE)
                    .edit().putString("state", root.toString()).apply();
        } catch (JSONException ignored) {
            Toast.makeText(this, "Local save failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray stringsToJson(Set<String> strings) {
        JSONArray array = new JSONArray();
        for (String value : strings) array.put(value);
        return array;
    }

    private List<String> sellerNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (Listing item : listings) if (!"YOU".equals(item.seller)) names.add(item.seller);
        names.addAll(chats.keySet());
        for (Review review : reviews) names.add(review.seller);
        if (names.isEmpty()) names.add("Grandpa Earl");
        ArrayList<String> sorted = new ArrayList<>(names);
        Collections.sort(sorted);
        return sorted;
    }

    private ArrayList<Review> reviewsFor(String seller) {
        ArrayList<Review> result = new ArrayList<>();
        for (Review review : reviews) if (seller.equals(review.seller)) result.add(review);
        return result;
    }

    private List<String> defaultReviews(String seller) {
        if ("Grandpa Earl".equals(seller)) return Arrays.asList("★★☆☆☆  THE PRODUCT LOOKED AT ME FUNNY.", "★☆☆☆☆  PICKUP ADDRESS WAS A SHED.");
        if ("Old Man Jenkins".equals(seller)) return Arrays.asList("★★☆☆☆  HE CALLED ME A WHIPPERSNAPPER.", "★☆☆☆☆  REMOTE CONTROL WAS A BRICK.");
        if ("Grandpa Walter".equals(seller)) return Arrays.asList("★★★☆☆  THE DENTURES STARTED TALKING.", "★☆☆☆☆  SPOON WAS TOO EXPENSIVE.");
        return Arrays.asList("★☆☆☆☆  MYSTERY BOX MYSTERIOUSLY YELLED AT ME.");
    }

    private Listing findListing(String id) {
        for (Listing item : listings) if (item.id.equals(id)) return item;
        return null;
    }

    private Integer parsePrice(String raw) {
        try {
            long value = Long.parseLong(raw.trim());
            if (value < 0 || value > 1_000_000_000L) return null;
            return (int) value;
        } catch (Exception exception) {
            return null;
        }
    }

    private void setStatus(String message) {
        if (statusBar != null) statusBar.setText(message);
    }

    private String stars(int count) {
        int safe = Math.max(0, Math.min(5, count));
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < safe; i++) value.append('★');
        for (int i = safe; i < 5; i++) value.append('☆');
        return value.toString();
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontal() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private LinearLayout card() {
        LinearLayout card = vertical();
        card.setPadding(dp(14), dp(13), dp(14), dp(13));
        card.setBackground(rounded(PANEL, 16, Color.rgb(64, 34, 45)));
        return card;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private TextView action(String value, int color, Runnable listener) {
        TextView button = text(value, 11, FG, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(11), dp(9), dp(11), dp(9));
        button.setMinHeight(dp(40));
        button.setBackground(rounded(color, 11, color == RED ? RED_BRIGHT : color));
        button.setClickable(true);
        button.setFocusable(true);
        button.setOnClickListener(view -> listener.run());
        return button;
    }

    private EditText edit(String hint, int inputType) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setHintTextColor(Color.rgb(161, 130, 140));
        field.setTextColor(FG);
        field.setTextSize(13);
        field.setSingleLine(inputType != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
        field.setInputType(inputType);
        field.setPadding(dp(13), dp(10), dp(13), dp(10));
        field.setBackground(rounded(PANEL_LIGHT, 11, Color.rgb(78, 39, 53)));
        return field;
    }

    private GradientDrawable rounded(int fill, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams topMargin(int top) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(top);
        return params;
    }

    private LinearLayout.LayoutParams bottomMargin(int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(bottom);
        return params;
    }

    private LinearLayout.LayoutParams leftMargin(int left) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.leftMargin = dp(left);
        return params;
    }

    private class Listing {
        final String id;
        final String name;
        final int price;
        final String seller;
        final String category;
        final String description;
        final int rating;

        Listing(String id, String name, int price, String seller, String category, String description, int rating) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.seller = seller;
            this.category = category;
            this.description = description;
            this.rating = rating;
        }

        Listing(JSONObject json) {
            id = json.optString("id", "0");
            name = json.optString("name", "Mystery Junk");
            price = Math.max(0, json.optInt("price", 0));
            seller = json.optString("seller", "Grandpa Earl");
            category = json.optString("category", "Misc");
            description = json.optString("description", "No description. Grandpa forgot.");
            rating = json.optInt("rating", 1);
        }

        int numericId() {
            try { return Integer.parseInt(id); } catch (NumberFormatException ignored) { return 0; }
        }

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("name", name);
            json.put("price", price);
            json.put("seller", seller);
            json.put("category", category);
            json.put("description", description);
            json.put("rating", rating);
            return json;
        }
    }

    private static class Message {
        final String sender;
        final String body;
        Message(String sender, String body) { this.sender = sender; this.body = body; }
        Message(JSONObject json) { sender = json.optString("sender", "Grandpa Earl"); body = json.optString("body", ""); }
        JSONObject toJson() throws JSONException { JSONObject j = new JSONObject(); j.put("sender", sender); j.put("body", body); return j; }
    }

    private static class Review {
        final String seller;
        final int rating;
        final String body;
        Review(String seller, int rating, String body) { this.seller = seller; this.rating = rating; this.body = body; }
        Review(JSONObject json) { seller = json.optString("seller", "Grandpa Earl"); rating = json.optInt("rating", 1); body = json.optString("body", ""); }
        JSONObject toJson() throws JSONException { JSONObject j = new JSONObject(); j.put("seller", seller); j.put("rating", rating); j.put("body", body); return j; }
    }

    private static class OfferRecord {
        final String item;
        final String seller;
        final int asking;
        final int offered;
        final String reply;
        OfferRecord(String item, String seller, int asking, int offered, String reply) { this.item = item; this.seller = seller; this.asking = asking; this.offered = offered; this.reply = reply; }
        OfferRecord(JSONObject json) { item = json.optString("item", "Mystery Junk"); seller = json.optString("seller", "Grandpa Earl"); asking = json.optInt("asking", 0); offered = json.optInt("offered", 0); reply = json.optString("reply", "I KNOW WHAT I GOT."); }
        JSONObject toJson() throws JSONException { JSONObject j = new JSONObject(); j.put("item", item); j.put("seller", seller); j.put("asking", asking); j.put("offered", offered); j.put("reply", reply); return j; }
    }

    private static class Receipt {
        final String name;
        final String seller;
        final int price;
        final long time;
        Receipt(String name, String seller, int price, long time) { this.name = name; this.seller = seller; this.price = price; this.time = time; }
        Receipt(JSONObject json) { name = json.optString("name", "Mystery Junk"); seller = json.optString("seller", "Grandpa Earl"); price = json.optInt("price", 0); time = json.optLong("time", 0); }
        JSONObject toJson() throws JSONException { JSONObject j = new JSONObject(); j.put("name", name); j.put("seller", seller); j.put("price", price); j.put("time", time); return j; }
    }
}
