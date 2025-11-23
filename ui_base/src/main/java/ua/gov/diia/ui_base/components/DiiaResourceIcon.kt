package ua.gov.diia.ui_base.components

import ua.gov.diia.ui_base.R

enum class DiiaResourceIcon(
    val code: String,
    val drawableResourceId: Int,
    val contentDescriptionResourceId: Int
) {
    UA("ua", R.drawable.ic_doc_translate_ua, R.string.context_menu),
    EN("en", R.drawable.ic_translate_eng, R.string.context_menu),
    MENU("menu", R.drawable.diia_ellipse_menu, R.string.context_menu),
    BACK("back", R.drawable.diia_back_arrow, R.string.not_handled_icon),
    ELLIPSE_ARROW_RIGHT(
        "ellipseArrowRight",
        R.drawable.ellipse_arrow_right,
        R.string.not_handled_icon
    ),
    ARROW_RIGHT("arrowRight", R.drawable.arrow_right, R.string.not_handled_icon),
    EAST_WHITE("eastWhite", R.drawable.ic_east_white, R.string.not_handled_icon),
    ARROW_FILL("arrowFill", R.drawable.ic_arrow_fill, R.string.not_handled_icon),
    ELLIPSE_CHECK("ellipseCheck", R.drawable.ellipse_check, R.string.not_handled_icon),
    ELLIPSE_INFO("ellipseInfo", R.drawable.ic_ellipse_info, R.string.ellipseInfo_icon_description),

    ELLIPSE_WHITE_ARROW_RIGHT(
        "ellipseWhiteArrowRight",
        R.drawable.ellipse_white_arrow_right,
        R.string.not_handled_icon
    ),
    PN("pn", R.drawable.pn, R.string.not_handled_icon),
    SHELTER("shelter", R.drawable.shelter, R.string.not_handled_icon),
    SAFETY("safety", R.drawable.safety, R.string.not_handled_icon),
    SAFETY_LARGE("safetyLarge", R.drawable.safety_large, R.string.not_handled_icon),
    DOUBLE_ICON("doubleIcon", R.drawable.double_icon, R.string.not_handled_icon),
    DEFAULT("default-stub", R.drawable.diia_check, R.string.not_handled_icon),
    DEFAULT_GLOBAL("default-global", R.drawable.ic_default_icon, R.string.not_handled_icon),
    ELLIPSE_KEBAB(
        "ellipseKebab",
        R.drawable.diia_ellipse_menu_black,
        R.string.ellipse_menu_icon_description
    ),
    QR_SCAN("qrScan", R.drawable.qr_scan, R.string.not_handled_icon),
    QR("qr", R.drawable.ic_doc_verification, R.string.not_handled_icon),
    QR_WHITE("qrWhite", R.drawable.ic_qr_white, R.string.not_handled_icon),
    BARCODE("barcode", R.drawable.ic_barcode, R.string.not_handled_icon),
    BARCODE_WHITE("barcodeWhite", R.drawable.ic_barcode_white, R.string.not_handled_icon),
    QR_SCAN_WHITE("qrScanWhite", R.drawable.qr_scan_white, R.string.not_handled_icon),
    TRIDENT("trident", R.drawable.trident, R.string.not_handled_icon),
    TRIDENT_WHITE("tridentWhite", R.drawable.trident_white, R.string.not_handled_icon),
    POLICE("police", R.drawable.ic_police, R.string.not_handled_icon),
    TARGET("target", R.drawable.target, R.string.not_handled_icon),
    TARGET_WHITE("targetWhite", R.drawable.target_white, R.string.not_handled_icon),
    STACK("stack", R.drawable.diia_ic_doc_stack, R.string.stack_icon_description),
    NOTIFICATION_NEW("notificationNew", R.drawable.notification_new, R.string.not_handled_icon),
    ELLIPSE_KEBAB_WHITE(
        "ellipseKebabWhite",
        R.drawable.ic_ellipse_white,
        R.string.not_handled_icon
    ),
    ELLIPSE_SETTINGS(
        "ellipseSettings",
        R.drawable.ic_notifications_top_bar_right,
        R.string.not_handled_icon
    ),
    ELLIPSE_CROSS("ellipseCross", R.drawable.ic_ellipse_cross, R.string.close),
    ELLIPSE_ATTENTION(
        "ellipseAttention",
        R.drawable.ic_ellipse_attention,
        R.string.not_handled_icon
    ),
    STACK_WHITE("stackWhite", R.drawable.ic_stack_white, R.string.not_handled_icon),
    SHARE("share", R.drawable.ic_share_rounded, R.string.not_handled_icon),
    HEATING("heating", R.drawable.diia_heating, R.string.not_handled_icon),
    GENERATOR("generator", R.drawable.diia_generator, R.string.not_handled_icon),
    CHARGING("charging", R.drawable.diia_charging, R.string.not_handled_icon),
    WATER("water", R.drawable.diia_water, R.string.not_handled_icon),
    BANKING("banking", R.drawable.diia_banking, R.string.not_handled_icon),
    POWER_BANKING("powerBanking", R.drawable.diia_banking, R.string.not_handled_icon),
    INTERNET_ACCESS("internetAccess", R.drawable.diia_internet, R.string.not_handled_icon),
    CELLULAR_CONNECTION("cellularConnection", R.drawable.diia_cellular, R.string.not_handled_icon),
    MEDICINE("medicine", R.drawable.diia_medicine, R.string.not_handled_icon),
    AID_KIT("aidKit", R.drawable.ic_aid_kit, R.string.not_handled_icon),
    EDIT("edit", R.drawable.ic_doc_edit_adress, R.string.not_handled_icon),
    CANCEL("cancel", R.drawable.ic_doc_cancel, R.string.not_handled_icon),
    CLOSE_RECTANGLE("closeRectangle", R.drawable.ic_close_rectangle, R.string.close_rectangle),
    BAG("bag", R.drawable.ic_bag, R.string.not_handled_icon),
    DELIVERY("delivery", R.drawable.ic_delivery, R.string.not_handled_icon),
    DOC_INFO("docInfo", R.drawable.ic_doc_info, R.string.not_handled_icon),
    OLD_DOC_INFO("oldDocInfo", R.drawable.ic_old_doc_info, R.string.not_handled_icon),
    HISTORY("history", R.drawable.ic_history, R.string.not_handled_icon),
    HOME_DOC("homedoc", R.drawable.ic_homedoc, R.string.not_handled_icon),
    HOME("home", R.drawable.ic_home, R.string.not_handled_icon),
    RATING("rating", R.drawable.ic_rating, R.string.not_handled_icon),
    SEARCH("search", R.drawable.ic_search_black, R.string.not_handled_icon),
    WALLET("wallet", R.drawable.ic_wallet, R.string.not_handled_icon),
    INFO("info", R.drawable.ic_info_about, R.string.not_handled_icon),
    EDUCATION("education", R.drawable.ic_education, R.string.not_handled_icon),
    DRAG("drag", R.drawable.drag, R.string.not_handled_icon),
    MESSAGE("message", R.drawable.ic_message, R.string.not_handled_icon),
    MESSAGE_CIRCLE("messageCircle", R.drawable.diia_ic_message_circle, R.string.not_handled_icon),
    NEW_MESSAGE("newMessage", R.drawable.new_message, R.string.not_handled_icon),
    GALLERY("gallery", R.drawable.diia_icon_gallery, R.string.not_handled_icon),
    CAMERA("camera", R.drawable.diia_icon_camera, R.string.not_handled_icon),
    HEART("heart", R.drawable.diia_ellipse_black_heart, R.string.not_handled_icon),
    RING("ring", R.drawable.ic_ring, R.string.not_handled_icon),
    NFC_STROKE("nfcStroke", R.drawable.ic_nfc_stroke, R.string.not_handled_icon),
    PLACEHOLDER("placeholder", R.drawable.diia_ic_placeholder, R.string.not_handled_icon),
    DEFAULT_ICON_LARGE(
        "defaultIconLarge",
        R.drawable.ic_default_icon_large,
        R.string.not_handled_icon
    ),
    CLOSE_LARGE("closeLarge", R.drawable.ic_close_large, R.string.not_handled_icon),
    ATTENTION("attention", R.drawable.ic_attention, R.string.not_handled_icon),
    SAVE("save", R.drawable.diia_ic_save, R.string.not_handled_icon),
    SAVE_FILL("saveFill", R.drawable.diia_ic_save_filled, R.string.not_handled_icon),
    CHECK_STROKE("checkStroke", R.drawable.diia_ic_check_stroke, R.string.not_handled_icon),
    SCANNER("scanner", R.drawable.diia_ic_scanner, R.string.not_handled_icon),

    //TODO change name for NOTIFICATION_MESSAGE
    NOTIFICATION_MESSAGE(
        "notificationMessage",
        R.drawable.ic_menu_notifications_action,
        R.string.not_handled_icon
    ),
    KEY("key", R.drawable.ic_key, R.string.not_handled_icon),
    BAG_KEY("bag_key", R.drawable.ic_bag_key, R.string.not_handled_icon),
    SETTINGS("settings", R.drawable.ic_settings, R.string.not_handled_icon),
    REFRESH("refresh", R.drawable.ic_doc_refresh, R.string.not_handled_icon),
    REORDER("reorder", R.drawable.ic_doc_reorder, R.string.not_handled_icon),
    DEVICE("device", R.drawable.ic_device, R.string.not_handled_icon),
    COPY("copy", R.drawable.ic_copy, R.string.copy_icon_description),
    COPY_WHITE("copy_white", R.drawable.ic_copy_white, R.string.not_handled_icon),
    FAQ("faq", R.drawable.ic_faq, R.string.not_handled_icon),
    ADD("add", R.drawable.ic_add, R.string.not_handled_icon),
    ICON_ADD("addIcon", R.drawable.ic_icon_add, R.string.not_handled_icon),
    DOWNLOAD("download", R.drawable.ic_download, R.string.not_handled_icon),
    OLD_DOWNLOAD("oldDownload", R.drawable.ic_download, R.string.not_handled_icon),
    DOWNLOAD_ICON("downloadIcon", R.drawable.ic_download_icon, R.string.not_handled_icon),
    DELETE("delete", R.drawable.ic_doc_delete, R.string.not_handled_icon),
    HIDE("hide", R.drawable.ic_hide, R.string.not_handled_icon),
    SHOW("show", R.drawable.ic_show, R.string.not_handled_icon),
    FILTER("filter", R.drawable.ic_filter, R.string.filter_icon_description),
    OLD_FAQ("oldFaq", R.drawable.ic_old_faq, R.string.not_handled_icon),
    CREATE_APPLICATION(
        "createApplication",
        R.drawable.ic_create_application,
        R.string.not_handled_icon
    ),
    APPLICATION_PROCESSING(
        "applicationProcessing",
        R.drawable.ic_application_processing,
        R.string.not_handled_icon
    ),
    MESSAGE_RECEIVING(
        "messageReceiving",
        R.drawable.ic_message_receiving,
        R.string.not_handled_icon
    ),
    DECLARATION_ARCHIVE(
        "declarationArchive",
        R.drawable.ic_declaration_archive,
        R.string.not_handled_icon
    ),
    DECLARATION_CARE("declarationCare", R.drawable.ic_declaration_care, R.string.not_handled_icon),
    CHECK_SHIELD("checkShield", R.drawable.ic_check_shield, R.string.not_handled_icon),
    TRANSITION_STATE("transitionState", R.drawable.ic_transition_state, R.string.not_handled_icon),
    ATTENTION_STATE("attentionState", R.drawable.ic_attention_state, R.string.not_handled_icon),
    SUCCESS_STATE("successState", R.drawable.ic_success_state, R.string.not_handled_icon),
    DONE_BLACK_ROUND(
        "doneBlackRound",
        R.drawable.diia_ic_done_black_round,
        R.string.not_handled_icon
    ),
    ARROW_BLACK_ROUND(
        "arrowBlackRound",
        R.drawable.diia_ic_arrow_black_round,
        R.string.not_handled_icon
    ),
    TIME_BLACK_ROUND(
        "timeBlackRound",
        R.drawable.diia_ic_time_black_round,
        R.string.not_handled_icon
    ),
    ATTENTION_BLACK_ROUND(
        "attentionBlackRound",
        R.drawable.diia_ic_attention_black_round,
        R.string.not_handled_icon
    ),
    ATTENTION_BLACK_ROUND_SMALL(
        "attentionBlackRoundSmall",
        R.drawable.ic_attention_black_round_small,
        R.string.not_handled_icon
    ),
    INFO_BLACK_ROUND(
        "infoBlackRound",
        R.drawable.diia_ic_info_black_round,
        R.string.not_handled_icon
    ),

    ARROW_LINK("arrowLink", R.drawable.ic_arrow_link, R.string.not_handled_icon),
    SEARCH_LENS("searchLens", R.drawable.ic_search_lens, R.string.not_handled_icon),
    ICON_DOWNLOAD("downloadIcon", R.drawable.ic_icon_download, R.string.not_handled_icon),
    ARROW_BACK("arrowBack", R.drawable.ic_arrow_back, R.string.not_handled_icon),

    BARCODE_SCAN("barcodeScan", R.drawable.diia_ic_barcode_scan, R.string.not_handled_icon),
    BARCODE_SCAN_DISABLED("barcodeScanDisabled", R.drawable.diia_ic_barcode_scan_disabled, R.string.not_handled_icon),
    CLEAR_INPUT("clearInput", R.drawable.diia_ic_clear_input, R.string.not_handled_icon),

    //TODO change name for SOME_DOCS
    SOME_DOCS("someDocs", R.drawable.ic_menu_history, R.string.not_handled_icon),
    PROFILE("profile", R.drawable.ic_profile, R.string.not_handled_icon),
    OUT_LINK("outLink", R.drawable.ic_arrow_top, R.string.not_handled_icon),
    SYRINGE("syringe", R.drawable.ic_syringe, R.string.not_handled_icon),
    UPDATE("update", R.drawable.ic_update, R.string.not_handled_icon),

    RETRY_WHITE("retry", R.drawable.ic_retry, R.string.not_handled_icon),
    UAEU_SWITCH("logoUaEu", R.drawable.ic_logouaeu, R.string.not_handled_icon),
    UA_LOGO("logoUA", R.drawable.ic_logoua, R.string.not_handled_icon),
    EU_LOGO("logoEU", R.drawable.ic_logoeu, R.string.not_handled_icon),

    USER_MALE("userMale", R.drawable.usermale, R.string.not_handled_icon),
    USER_FEMALE("userFemale", R.drawable.userfemale, R.string.not_handled_icon),
    ARROW_MIN_RIGHT("arrowSliderRight", R.drawable.arrow_min_right, R.string.not_handled_icon),
    ARROW_MIN_LEFT("arrowSliderLeft", R.drawable.arrow_min_left, R.string.not_handled_icon),
    ARROW_SLIDER_DOWN(
        "arrowSliderDown",
        R.drawable.diia_ic_arrow_slider_down,
        R.string.not_handled_icon
    ),
    CHEVRON_DOWN("chevronDown", R.drawable.ic_chevron_down, R.string.not_handled_icon),
    CHEVRON_UP("chevronUp", R.drawable.ic_chevron_up, R.string.not_handled_icon),
    CHEVRON_SMALL_RIGHT("chevronSmallRight", R.drawable.ic_chevron_small_right, R.string.not_handled_icon),
    MIC_ON("micOnWhite", R.drawable.ic_mic_on, R.string.not_handled_icon),
    MIC_OFF("micOffWhite", R.drawable.ic_mic_off, R.string.not_handled_icon),
    SPEAKER_ON("speakerOnWhite", R.drawable.ic_speaker_on, R.string.not_handled_icon),
    SPEAKER_OFF("speakerOffWhite", R.drawable.ic_speaker_off, R.string.not_handled_icon),
    HEADPHONES("headphonesWhite", R.drawable.ic_headphones, R.string.not_handled_icon),
    BLUETOOTH("bluetoothWhite", R.drawable.ic_bluetooth, R.string.not_handled_icon),
    CLOSE("closeWhite", R.drawable.ic_close, R.string.not_handled_icon),
    CLOSE_WHITE("closeWhite", R.drawable.ic_close_white, R.string.not_handled_icon),
    FLIP_CAMERA("flipCamera", R.drawable.ic_flip_camera, R.string.not_handled_icon),
    MADE_IN_UA("madeInUa", R.drawable.ic_madeinua, R.string.not_handled_icon),
    CHARGE_CASH("chargeCash", R.drawable.ic_charge_cash, R.string.not_handled_icon),
    PAYMENT_CASH("paymentCash", R.drawable.ic_paymentcash, R.string.not_handled_icon),
    CASHBACK_CHARGE("cashbackCharge", R.drawable.ic_cashback_charge, R.string.not_handled_icon),
    CASHBACK_RETURN("cashbackReturn", R.drawable.ic_cashback_return, R.string.not_handled_icon),
    CASHBACK_PAYMENT("cashbackPayment", R.drawable.ic_cashback_payment, R.string.not_handled_icon),
    DONE_BLACK_SQUARE(
        "doneBlackSquare",
        R.drawable.ic_done_black_square,
        R.string.not_handled_icon
    ),
    TIME_BLACK_SQUARE(
        "timeBlackSquare",
        R.drawable.ic_time_black_square,
        R.string.not_handled_icon
    ),
    INFO_BLACK_SQUARE(
        "infoBlackSquare",
        R.drawable.ic_info_black_square,
        R.string.not_handled_icon
    ),
    ATTENTION_BLACK_SQUARE(
        "attentionBlackSquare",
        R.drawable.ic_attention_black_square,
        R.string.not_handled_icon
    ),
    CARD_VISA("cardVisa", R.drawable.ic_card_visa, R.string.not_handled_icon),
    CARD_MASTERCARD("cardMastercard", R.drawable.ic_card_mastercard, R.string.not_handled_icon),
    CARD_PROSTIR("cardProstir", R.drawable.ic_card_prostir, R.string.not_handled_icon),
    LOGO_VISA_WHITE("logoVisaWhite", R.drawable.ic_payment_logo_visa, R.string.logo_visa_white_icon_description),
    LOGO_PROSTIR_WHITE(
        "logoProstirWhite",
        R.drawable.ic_logo_prostir_white,
        R.string.not_handled_icon
    ),
    LOGO_MASTER_CARD("logoMC", R.drawable.ic_logo_mc, R.string.logo_mc_icon_description),
    SENSE_BANK("senseBankLogo", R.drawable.ic_sense_bank, R.string.sense_bank_logo_icon_description),
    PRIVAT_BANK("privatBankLogo", R.drawable.ic_privat24, R.string.privat_bank_logo_icon_description),
    MONO_BANK("monoBankLogo", R.drawable.ic_mono_bank, R.string.mono_bank_logo_icon_description),
    RAIF_BANK("raiffeisenBankLogo", R.drawable.ic_raif_bank, R.string.raif_bank_logo_icon_description),
    ABANK("aBankLogo", R.drawable.ic_abank, R.string.abank_logo_icon_description),
    UKRGAZ_BANK("ukrgazBankLogo", R.drawable.ic_ukrgaz_bank, R.string.ukrgaz_bank_logo_icon_description),
    CREDIT_DNIPRO_BANK(
        "creditDniproBankLogo",
        R.drawable.ic_credit_dnipro_bank,
        R.string.credit_dnipro_bank_logo_icon_description
    ),
    GLOBUS_BANK("globusBankLogo", R.drawable.ic_globus_bank, R.string.globus_bank_logo_icon_description),
    MTB_BANK("mtbBankLogo", R.drawable.ic_mtb_bank, R.string.mtb_bank_logo_icon_description),
    PUMB_BANK("pumbLogo", R.drawable.ic_pumb_bank, R.string.pumb_bank_logo_icon_description),
    UKREXIM_BANK("ukreximBankLogo", R.drawable.ic_ukrexim_bank, R.string.ukrexim_bank_logo_icon_description),
    UKRSIB_BANK("ukrsibBankLogo", R.drawable.ic_ukrsib_bank, R.string.ukrsib_bank_logo_icon_description),
    OSCHAD_BANK("oschadBankLogo", R.drawable.ic_oschad_bank, R.string.oschad_bank_logo_icon_description),
    UNIVERSAL_BANK("universalBankLogo", R.drawable.ic_universal_bank, R.string.universal_bank_logo_icon_description),
    OTP_BANK("otpBankLogo", R.drawable.ic_otp_bank, R.string.otp_bank_logo_icon_description),
    VOSTOK_BANK("vostokBankLogo", R.drawable.ic_vostok_bank, R.string.vostok_bank_logo_icon_description),
    TASKOM_BANK("taskomBankLogo", R.drawable.ic_taskom_bank, R.string.taskom_bank_logo_icon_description),
    IZI_BANK("iziBankLogo", R.drawable.ic_izi_bank, R.string.izi_bank_logo_icon_description),
    PIVDENNYI_BANK("pivdennyiBankLogo", R.drawable.ic_pivdennyi_bank, R.string.pivdennyi_bank_logo_icon_description),
    KREDO_BANK("kredoBankLogo", R.drawable.ic_kredo_bank, R.string.kredo_bank_logo_icon_description),
    CREDITAGRICOL_BANK(
        "creditagricoleBankLogo",
        R.drawable.ic_creditagricole_bank,
        R.string.creditagricole_bank_logo_icon_description
    ),
    IDEA_BANK("ideaBankLogo", R.drawable.ic_idea_bank, R.string.idea_bank_logo_icon_description),
    ACCORD_BANK("accordBankLogo", R.drawable.ic_accord_bank, R.string.accord_bank_logo_icon_description),
    UNEX_BANK("unexBankLogo", R.drawable.ic_unex_bank, R.string.unex_bank_logo_icon_description),
    POLTAVA_BANK("poltavaBankLogo", R.drawable.ic_poltava_bank, R.string.poltava_bank_logo_icon_description),
    CLEARINGHOUSE_BANK(
        "clearinghouseBankLogo",
        R.drawable.ic_clearinghouse_bank,
        R.string.clearinghouse_bank_logo_icon_description
    ),
    LVIV_BANK("lvivBankLogo", R.drawable.ic_lviv_bank, R.string.lviv_bank_logo_icon_description),
    PRAVEX_BANK("pravexBankLogo", R.drawable.ic_pravex_bank, R.string.pravex_bank_logo_icon_description),
    PIRAEUS_BANK("piraeusBankLogo", R.drawable.ic_piraeus_bank, R.string.piraeus_bank_logo_icon_description),
    INDUSTRIAL_BANK("industrialBankLogo", R.drawable.ic_industrial_bank, R.string.industrial_bank_logo_icon_description),
    RADA_BANK("radaBankLogo", R.drawable.ic_rada_bank, R.string.rada_bank_logo_icon_description),
    COMIN_BANK("cominBankLogo", R.drawable.ic_comin_bank, R.string.comin_bank_logo_icon_description),
    ALLIANCE_BANK("allianceBankLogo", R.drawable.ic_alliance_bank, R.string.alliance_bank_logo_icon_description),
    RWS_BANK("rwsBankLogo", R.drawable.ic_rws_bank, R.string.rws_bank_logo_icon_description),
    BIS_BANK("bisBankLogo", R.drawable.ic_bis_bank, R.string.bis_bank_logo_icon_description),
    ASVIO_BANK("asvioBankLogo", R.drawable.ic_asvio_bank, R.string.asvio_bank_logo_icon_description),
    MOTOR_BANK("motorBankLogo", R.drawable.ic_motor_bank, R.string.motor_bank_logo_icon_description),
    KRYSTAL_BANK("krystalBankLogo", R.drawable.ic_krystal_bank, R.string.krystal_bank_logo_icon_description),
    GRANT_BANK("grantBankLogo", R.drawable.ic_grant_bank, R.string.grant_bank_logo_icon_description),
    ALT_BANK("altBankLogo", R.drawable.ic_alt_bank, R.string.alt_bank_logo_icon_description),
    BANK34_BANK("bank34BankLogo", R.drawable.ic_bank34_bank, R.string.bank34_bank_logo_icon_description),
    SKY_BANK("skyBankLogo", R.drawable.ic_sky_bank, R.string.sky_bank_logo_icon_description),
    CARD_TYPE_MC("cardTypeMc", R.drawable.ic_card_type_mc, R.string.not_handled_icon),
    CARD_TYPE_VISA_BLUE(
        "cardTypeVisaBlue",
        R.drawable.ic_card_type_visa_blue,
        R.string.not_handled_icon
    ),
    CARD_TYPE_PROSTIR(
        "cardTypeProstir",
        R.drawable.ic_card_type_prostir,
        R.string.not_handled_icon
    ),
    EMOJI_FLAG_UA("emojiFlagUA", R.drawable.ic_emoji_flag_ua, R.string.not_handled_icon),
    KEYBOARD_SCAN("keyboardScan", R.drawable.ic_keyboard_scan, R.string.not_handled_icon),
    FLASH_SCAN("flashScan", R.drawable.ic_flash_scan, R.string.not_handled_icon),
    FLASH_SCAN_WHITE("flashScanWhite", R.drawable.ic_flash_scan_white, R.string.not_handled_icon),
    FAILED_CONNECTION(
        "failedConnection",
        R.drawable.ic_failed_connection,
        R.string.not_handled_icon
    ),
    INVINCIBILITY_ALL("safety", R.drawable.ic_chip_all, R.string.not_handled_icon),
    INVINCIBILITY_POINTS("pn", R.drawable.ic_chip_point, R.string.not_handled_icon),
    INVINCIBILITY_SHELTERS("shelter", R.drawable.ic_chip_shelter, R.string.not_handled_icon),
    MEDIUM_PLUS("mediumPlus", R.drawable.ic_medium_plus, R.string.medium_plus_icon_description),
    WAITING_ICON("mediumWaiting", R.drawable.ic_waiting_icon, R.string.not_handled_icon),
    LOGO_DIIA("logoDiia", R.drawable.ic_logo_diia, R.string.logo_diia_icon_description),
    LOGO_DIIA_STROKE("logoDiiaStroke", R.drawable.ic_logo_diia_stroke, R.string.logo_diia_stroke_icon_description),

    CARD_TYPE__MC("card-type--mc", R.drawable.ic_card_type_mc, R.string.not_handled_icon),
    CARD_TYPE__PROSTIR(
        "card-type--prostir",
        R.drawable.ic_card_type__prostir,
        R.string.not_handled_icon
    ),
    CARD_TYPE__VISA(
        "card-type--visa",
        R.drawable.ic_card_type_visa_blue,
        R.string.not_handled_icon
    ),
    PLUS_BLACK_ROUND("plusBlackRound", R.drawable.ic_plus_black_round, R.string.not_handled_icon),
    ;


    companion object {
        fun getResourceId(code: String?): Int {
            return DiiaResourceIcon.values().firstOrNull { code == it.code }?.drawableResourceId
                ?: DEFAULT_GLOBAL.drawableResourceId
        }

        fun getContentDescription(code: String): Int {
            return DiiaResourceIcon.values()
                .firstOrNull { code == it.code }?.contentDescriptionResourceId
                ?: DEFAULT_GLOBAL.contentDescriptionResourceId
        }
    }

}
