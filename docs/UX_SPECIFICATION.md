# MedChart EHR - UX Specification

## Overview

This document defines the user experience specifications for MedChart EHR, ensuring a consistent, efficient, and clinically-appropriate interface that mirrors real-world EHR systems like Epic, Cerner, and Athena.

---

## 1. Global Navigation & Layout

### 1.1 Application Shell

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ ┌─────┐  MedChart EHR          🔍 Global Search    📥 Inbox(12)  👤 Dr. Chen ▼│
│ │ ≡   │                        _______________     [Results: 5]              │
│ └─────┘                                            [Messages: 4]             │
│                                                    [Tasks: 3]                │
├─────────────────────────────────────────────────────────────────────────────┤
│  📅 Schedule  │  👥 Patients  │  ✉️ Messages  │  ✓ Tasks  │  📊 Reports     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│                         [ MAIN CONTENT AREA ]                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Global Search

- **Trigger**: `Ctrl/Cmd + K` or click search bar
- **Search Types**:
  - Patients (by name, MRN, DOB, phone)
  - Providers
  - Medications
  - Diagnoses (ICD-10)
  - Help articles
- **Recent searches** displayed on focus
- **Type-ahead suggestions** with category indicators

### 1.3 Inbox/Notifications

```
┌─────────────────────────────────────────┐
│ 📥 Inbox                          ✕    │
├─────────────────────────────────────────┤
│ ┌─ Results (5) ─────────────────────┐  │
│ │ 🔴 CRITICAL: K+ 6.8 - Smith, John │  │
│ │ 🟡 Abnormal: HbA1c 9.2 - Garcia   │  │
│ │ ⚪ Normal: CBC - Wilson, Mary     │  │
│ │ ⚪ Normal: CMP - Brown, Robert    │  │
│ │ ⚪ Pending: MRI Brain - Davis     │  │
│ └───────────────────────────────────┘  │
│ ┌─ Messages (4) ────────────────────┐  │
│ │ Patient: Refill request - Jones   │  │
│ │ Staff: Prior auth needed - Lee    │  │
│ │ Referral: Consult note - Cardio   │  │
│ │ System: License expiring in 30d   │  │
│ └───────────────────────────────────┘  │
│ ┌─ Tasks (3) ───────────────────────┐  │
│ │ ⬜ Sign note - Smith, John        │  │
│ │ ⬜ Review labs - Garcia, Maria    │  │
│ │ ⬜ Call patient - Wilson, Mary    │  │
│ └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

---

## 2. Schedule View

### 2.1 Daily Schedule

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📅 Schedule                                          ◀ Jan 18, 2024 ▶  Today │
├─────────────────────────────────────────────────────────────────────────────┤
│ View: [Day] Week Month  │  Provider: Dr. Chen ▼  │  Location: Main Clinic ▼ │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  8:00 AM  ┌─────────────────────────────────────────────────────────────┐  │
│           │ ✅ Smith, John (58M)                              [Chart]   │  │
│           │ Annual Physical | Room 3 | Checked In 7:52 AM              │  │
│           │ ⚠️ Allergies: Penicillin | 🔴 Diabetes, HTN                 │  │
│           └─────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  8:30 AM  ┌─────────────────────────────────────────────────────────────┐  │
│           │ 🟡 Garcia, Maria (45F)                            [Chart]   │  │
│           │ Diabetes Follow-up | Scheduled                              │  │
│           │ Last A1c: 7.8% (3 months ago)                               │  │
│           └─────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  9:00 AM  ┌─────────────────────────────────────────────────────────────┐  │
│           │ 🔵 Wilson, Mary (67F)                             [Chart]   │  │
│           │ New Patient | In Progress                                   │  │
│           │ Chief Complaint: Chest pain x 2 weeks                       │  │
│           └─────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  9:30 AM  ┌─────────────────────────────────────────────────────────────┐  │
│           │ ⬜ -- AVAILABLE --                                          │  │
│           │ [+ Schedule Appointment]                                    │  │
│           └─────────────────────────────────────────────────────────────┘  │
│                                                                             │
│  10:00 AM ┌─────────────────────────────────────────────────────────────┐  │
│           │ ❌ Brown, Robert (72M)                            [Chart]   │  │
│           │ Follow-up | NO SHOW                                         │  │
│           │ [Reschedule] [Document No-Show]                             │  │
│           └─────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘

Status Legend:
✅ Checked In    🟡 Scheduled    🔵 In Progress    
✓ Completed      ❌ No Show      ⊘ Cancelled
```

### 2.2 Appointment Actions

Right-click or action menu on appointment:
- Check In
- Start Visit / Room Patient
- Open Chart
- Reschedule
- Cancel (with reason)
- Mark No-Show
- Print Face Sheet
- Send Reminder

---

## 3. Patient Chart

### 3.1 Patient Header (Sticky)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 👤 Smith, John Robert                                                       │
│ MRN: 12345678 │ DOB: 03/15/1965 (58y) │ M │ English                        │
├─────────────────────────────────────────────────────────────────────────────┤
│ ⚠️ ALLERGIES: Penicillin (Anaphylaxis), Sulfa (Rash)                       │
│ 🚨 ALERTS: Fall Risk │ Diabetes │ Controlled Substance Agreement           │
├─────────────────────────────────────────────────────────────────────────────┤
│ PCP: Dr. Sarah Chen │ 📞 (555) 123-4567 │ 📧 john.smith@email.com          │
│ Insurance: Blue Cross PPO │ Last Visit: 01/15/2024                         │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Chart Navigation (Left Sidebar)

```
┌──────────────────┐
│ 📋 CHART         │
├──────────────────┤
│ ▸ Summary        │  ← Dashboard view
│ ▸ Encounters     │  ← Visit history
│ ▸ Problems       │  ← Problem list
│ ▸ Medications    │  ← Med list + history
│ ▸ Allergies      │  ← Allergy list
│ ▸ Vitals         │  ← Vital signs
│ ▸ Notes          │  ← Clinical notes
│ ▸ Orders         │  ← Active orders
│ ▸ Results        │  ← Lab/imaging results
│ ▸ Immunizations  │  ← Vaccine record
│ ▸ Documents      │  ← Scanned docs
│ ▸ History        │  ← Social/Family hx
│ ▸ Care Team      │  ← Providers
│ ▸ Insurance      │  ← Coverage info
├──────────────────┤
│ 📝 DOCUMENTATION │
├──────────────────┤
│ [+ New Note]     │
│ [+ New Order]    │
│ [+ Message]      │
└──────────────────┘
```

### 3.3 Chart Summary (Dashboard)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ CHART SUMMARY                                              [Customize View] │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ ┌─ Active Problems ──────────────────┐ ┌─ Medications ──────────────────┐  │
│ │ • E11.9 Type 2 Diabetes (2019)     │ │ • Metformin 1000mg BID        │  │
│ │ • I10 Hypertension (2018)          │ │ • Lisinopril 20mg daily       │  │
│ │ • E78.5 Hyperlipidemia (2020)      │ │ • Atorvastatin 40mg QHS       │  │
│ │ • M54.5 Low back pain (2023)       │ │ • Aspirin 81mg daily          │  │
│ │                          [View All]│ │                     [View All]│  │
│ └────────────────────────────────────┘ └────────────────────────────────┘  │
│                                                                             │
│ ┌─ Recent Vitals (01/15/2024) ───────┐ ┌─ Allergies ────────────────────┐  │
│ │ BP: 142/88 mmHg        🔴 High     │ │ 🔴 Penicillin - Anaphylaxis    │  │
│ │ HR: 72 bpm             ✓ Normal    │ │ 🟡 Sulfa drugs - Rash          │  │
│ │ Temp: 98.6°F           ✓ Normal    │ │ 🟢 Latex - Skin irritation     │  │
│ │ SpO2: 98%              ✓ Normal    │ │                                │  │
│ │ Weight: 195 lbs        BMI: 28.0   │ │                                │  │
│ │                        [+ Record]  │ │                      [+ Add]   │  │
│ └────────────────────────────────────┘ └────────────────────────────────┘  │
│                                                                             │
│ ┌─ Recent Results ───────────────────┐ ┌─ Upcoming ─────────────────────┐  │
│ │ 01/15 HbA1c: 7.8% (goal <7%)  🟡   │ │ 02/15 Diabetes Follow-up      │  │
│ │ 01/15 Creatinine: 1.1 mg/dL   ✓    │ │ 03/01 Annual Eye Exam         │  │
│ │ 01/15 LDL: 98 mg/dL           ✓    │ │                               │  │
│ │ 01/10 Urine Microalbumin: Neg ✓    │ │                               │  │
│ │                          [View All]│ │                  [+ Schedule] │  │
│ └────────────────────────────────────┘ └────────────────────────────────┘  │
│                                                                             │
│ ┌─ Care Gaps / Reminders ────────────────────────────────────────────────┐ │
│ │ ⚠️ Overdue: Diabetic foot exam (last: 14 months ago)                   │ │
│ │ ⚠️ Overdue: Colonoscopy (age 58, no record)                            │ │
│ │ 📅 Due Soon: Flu vaccine (recommended annually)                        │ │
│ │ ✓ Up to date: Pneumonia vaccine (2022)                                 │ │
│ └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│ ┌─ Recent Encounters ────────────────────────────────────────────────────┐ │
│ │ 01/15/2024 │ Office Visit │ Dr. Chen │ Diabetes follow-up    [Open]   │ │
│ │ 10/20/2023 │ Office Visit │ Dr. Chen │ Annual physical       [Open]   │ │
│ │ 07/12/2023 │ Urgent Care  │ Dr. Kim  │ Back pain             [Open]   │ │
│ └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.4 Problem List View

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ PROBLEM LIST                                    [+ Add Problem] [⚙️ Filter] │
├─────────────────────────────────────────────────────────────────────────────┤
│ Show: [●Active] [○Resolved] [○All]  │  Sort: [Onset Date ▼]                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ ┌─ Active Problems ─────────────────────────────────────────────────────┐  │
│ │                                                                       │  │
│ │ ☐ │ E11.9 │ Type 2 diabetes mellitus      │ Onset: 03/2019 │ Chronic │  │
│ │   │       │ without complications          │ Dr. Chen       │         │  │
│ │   │       │ Last A1c: 7.8% (01/15/2024)   │                │ [Edit]  │  │
│ │   ├───────┼───────────────────────────────┼────────────────┼─────────│  │
│ │ ☐ │ I10   │ Essential hypertension        │ Onset: 06/2018 │ Chronic │  │
│ │   │       │                                │ Dr. Chen       │         │  │
│ │   │       │ Last BP: 142/88 (01/15/2024)  │                │ [Edit]  │  │
│ │   ├───────┼───────────────────────────────┼────────────────┼─────────│  │
│ │ ☐ │ E78.5 │ Hyperlipidemia, unspecified   │ Onset: 01/2020 │ Chronic │  │
│ │   │       │                                │ Dr. Chen       │         │  │
│ │   │       │ Last LDL: 98 (01/15/2024)     │                │ [Edit]  │  │
│ │   ├───────┼───────────────────────────────┼────────────────┼─────────│  │
│ │ ☐ │ M54.5 │ Low back pain                 │ Onset: 07/2023 │ Acute   │  │
│ │   │       │                                │ Dr. Kim        │         │  │
│ │   │       │ Improving with PT             │                │ [Edit]  │  │
│ │                                                                       │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ Resolved Problems ───────────────────────────────────────────────────┐  │
│ │ ☐ │ J06.9 │ Upper respiratory infection   │ 12/2023-12/2023│ Resolved│  │
│ │ ☐ │ K21.0 │ GERD                          │ 2020-2022      │ Resolved│  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ [Mark Resolved] [Delete] [Print]                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.5 Medications View

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ MEDICATIONS                              [+ New Rx] [Reconcile] [E-Prescribe]│
├─────────────────────────────────────────────────────────────────────────────┤
│ View: [●Active] [○All] [○History]  │  [🔍 Search medications...]           │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ ┌─ Active Medications ──────────────────────────────────────────────────┐  │
│ │                                                                       │  │
│ │ 💊 Metformin HCl 1000mg tablet                                       │  │
│ │    Take 1 tablet by mouth twice daily with meals                     │  │
│ │    Prescribed: 01/15/2024 │ Dr. Chen │ Qty: 180 │ Refills: 3         │  │
│ │    For: Type 2 Diabetes                                              │  │
│ │    [Refill] [Modify] [Discontinue] [History]                         │  │
│ │ ─────────────────────────────────────────────────────────────────────│  │
│ │ 💊 Lisinopril 20mg tablet                                            │  │
│ │    Take 1 tablet by mouth once daily                                 │  │
│ │    Prescribed: 01/15/2024 │ Dr. Chen │ Qty: 90 │ Refills: 3          │  │
│ │    For: Hypertension                                                 │  │
│ │    [Refill] [Modify] [Discontinue] [History]                         │  │
│ │ ─────────────────────────────────────────────────────────────────────│  │
│ │ 💊 Atorvastatin 40mg tablet                                          │  │
│ │    Take 1 tablet by mouth at bedtime                                 │  │
│ │    Prescribed: 01/15/2024 │ Dr. Chen │ Qty: 90 │ Refills: 3          │  │
│ │    For: Hyperlipidemia                                               │  │
│ │    [Refill] [Modify] [Discontinue] [History]                         │  │
│ │ ─────────────────────────────────────────────────────────────────────│  │
│ │ 💊 Aspirin 81mg tablet (OTC)                                         │  │
│ │    Take 1 tablet by mouth once daily                                 │  │
│ │    Started: 06/2020 │ Patient reported                               │  │
│ │    For: Cardiovascular protection                                    │  │
│ │    [Edit] [Remove]                                                   │  │
│ │                                                                       │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ PRN Medications ─────────────────────────────────────────────────────┐  │
│ │ 💊 Ibuprofen 400mg tablet                                            │  │
│ │    Take 1 tablet by mouth every 6 hours as needed for pain           │  │
│ │    Prescribed: 07/12/2023 │ Dr. Kim │ For: Back pain                 │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ⚠️ Drug Interactions: None detected                                        │
│ ⚠️ Allergy Alerts: Avoid penicillin-class antibiotics                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.6 Results View

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ RESULTS                                              [Filter ▼] [Date Range]│
├─────────────────────────────────────────────────────────────────────────────┤
│ View: [●All] [○Labs] [○Imaging] [○Other]  │  Status: [●All] [○Unreviewed]  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ ┌─ January 15, 2024 ────────────────────────────────────────────────────┐  │
│ │                                                                       │  │
│ │ 🧪 Comprehensive Metabolic Panel                    ✓ Reviewed        │  │
│ │ ┌─────────────────────────────────────────────────────────────────┐  │  │
│ │ │ Test            │ Result  │ Flag │ Reference    │ Prior        │  │  │
│ │ ├─────────────────┼─────────┼──────┼──────────────┼──────────────│  │  │
│ │ │ Glucose         │ 128     │ 🔴 H │ 70-100 mg/dL │ 135 (10/23)  │  │  │
│ │ │ BUN             │ 18      │      │ 7-20 mg/dL   │ 16           │  │  │
│ │ │ Creatinine      │ 1.1     │      │ 0.7-1.3 mg/dL│ 1.0          │  │  │
│ │ │ Sodium          │ 140     │      │ 136-145 mEq/L│ 141          │  │  │
│ │ │ Potassium       │ 4.2     │      │ 3.5-5.0 mEq/L│ 4.0          │  │  │
│ │ │ CO2             │ 24      │      │ 23-29 mEq/L  │ 25           │  │  │
│ │ │ Calcium         │ 9.5     │      │ 8.5-10.5mg/dL│ 9.4          │  │  │
│ │ │ Total Protein   │ 7.0     │      │ 6.0-8.3 g/dL │ 7.1          │  │  │
│ │ │ Albumin         │ 4.0     │      │ 3.5-5.0 g/dL │ 4.1          │  │  │
│ │ │ Bilirubin       │ 0.8     │      │ 0.1-1.2 mg/dL│ 0.7          │  │  │
│ │ │ Alk Phos        │ 72      │      │ 44-147 U/L   │ 68           │  │  │
│ │ │ AST             │ 25      │      │ 10-40 U/L    │ 22           │  │  │
│ │ │ ALT             │ 28      │      │ 7-56 U/L     │ 24           │  │  │
│ │ │ eGFR            │ 78      │      │ >60 mL/min   │ 82           │  │  │
│ │ └─────────────────────────────────────────────────────────────────┘  │  │
│ │ [📈 Trend] [📄 Print] [📧 Send to Patient]                           │  │
│ │                                                                       │  │
│ │ 🧪 Hemoglobin A1c                                   ✓ Reviewed        │  │
│ │ │ HbA1c           │ 7.8%    │ 🟡   │ <7.0%        │ 8.2% (10/23) │  │  │
│ │ [📈 Trend] [📄 Print]                                                │  │
│ │                                                                       │  │
│ │ 🧪 Lipid Panel                                      ✓ Reviewed        │  │
│ │ │ Total Chol      │ 185     │      │ <200 mg/dL   │ 210          │  │  │
│ │ │ LDL             │ 98      │      │ <100 mg/dL   │ 125          │  │  │
│ │ │ HDL             │ 52      │      │ >40 mg/dL    │ 48           │  │  │
│ │ │ Triglycerides   │ 145     │      │ <150 mg/dL   │ 180          │  │  │
│ │                                                                       │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ October 20, 2023 ────────────────────────────────────────────────────┐  │
│ │ 📷 Chest X-Ray PA and Lateral                       ✓ Reviewed        │  │
│ │ IMPRESSION: No acute cardiopulmonary disease.                        │  │
│ │ [View Full Report] [View Images]                                     │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Clinical Documentation

### 4.1 Note Editor

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 📝 Progress Note - Smith, John                    [Save Draft] [Sign Note]  │
├─────────────────────────────────────────────────────────────────────────────┤
│ Template: [Office Visit - Follow-up ▼]  │  [Insert SmartText ▼]            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ ┌─ Visit Info ──────────────────────────────────────────────────────────┐  │
│ │ Date: 01/18/2024    Provider: Dr. Sarah Chen    Type: Follow-up      │  │
│ │ Chief Complaint: [Diabetes follow-up, medication refills            ]│  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ Subjective ──────────────────────────────────────────────────────────┐  │
│ │ Patient presents for routine diabetes follow-up. Reports good        │  │
│ │ compliance with medications. Checking blood sugars 2-3x weekly,      │  │
│ │ fasting values 120-140. No hypoglycemic episodes. Denies polyuria,   │  │
│ │ polydipsia, or blurred vision.                                       │  │
│ │                                                                       │  │
│ │ [.diabetesfu] [.medcompliance] [.hypoglycemia]  ← SmartText triggers │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ Objective ───────────────────────────────────────────────────────────┐  │
│ │ ┌─ Vitals (auto-imported) ─────────────────────────────────────────┐ │  │
│ │ │ BP: 142/88 │ HR: 72 │ Temp: 98.6 │ SpO2: 98% │ Wt: 195 lbs      │ │  │
│ │ └──────────────────────────────────────────────────────────────────┘ │  │
│ │                                                                       │  │
│ │ Physical Exam:                                                        │  │
│ │ General: Alert, oriented, no acute distress                          │  │
│ │ HEENT: PERRLA, EOMI, oropharynx clear                               │  │
│ │ Cardiovascular: RRR, no murmurs                                      │  │
│ │ Lungs: Clear to auscultation bilaterally                            │  │
│ │ Extremities: No edema, dorsalis pedis pulses 2+ bilaterally         │  │
│ │ Neuro: Monofilament sensation intact bilateral feet                  │  │
│ │                                                                       │  │
│ │ [.normalexam] [.diabeticfoot]  ← SmartText triggers                  │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ Assessment ──────────────────────────────────────────────────────────┐  │
│ │ 1. E11.9 Type 2 diabetes mellitus - improving, A1c down from 8.2%   │  │
│ │    to 7.8%. Continue current regimen.                    [+ Problem] │  │
│ │ 2. I10 Essential hypertension - suboptimally controlled, BP 142/88  │  │
│ │    Consider increasing lisinopril.                       [+ Problem] │  │
│ │ 3. E78.5 Hyperlipidemia - at goal, LDL 98                [+ Problem] │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ Plan ────────────────────────────────────────────────────────────────┐  │
│ │ 1. Diabetes: Continue metformin 1000mg BID. Recheck A1c in 3 months.│  │
│ │    Reinforce diet and exercise.                                      │  │
│ │ 2. Hypertension: Increase lisinopril to 40mg daily.     [+ Order]   │  │
│ │ 3. Hyperlipidemia: Continue atorvastatin 40mg QHS.                  │  │
│ │ 4. Health maintenance: Schedule colonoscopy (overdue).              │  │
│ │ 5. Follow-up in 3 months.                               [+ Appt]    │  │
│ │                                                                       │  │
│ │ Orders placed this visit:                                            │  │
│ │ • Lisinopril 40mg tablet - 1 PO daily - Qty 90, 3 refills           │  │
│ │ • HbA1c - in 3 months                                                │  │
│ │ • CMP - in 3 months                                                  │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ Patient Instructions ────────────────────────────────────────────────┐  │
│ │ [✓] Diabetes education handout                                       │  │
│ │ [✓] Low sodium diet                                                  │  │
│ │ [ ] Exercise recommendations                                         │  │
│ │ [Print AVS] [Send to Portal]                                         │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ [Save Draft]  [Preview]  [Sign Note]  │  Auto-saved 2 minutes ago          │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 Order Entry Modal

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 💊 New Medication Order                                              [✕]   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│ Medication: [🔍 lisinopril                                              ]  │
│             ┌────────────────────────────────────────────────────────┐     │
│             │ Lisinopril 5mg tablet                                  │     │
│             │ Lisinopril 10mg tablet                                 │     │
│             │ Lisinopril 20mg tablet                                 │     │
│             │ ▶ Lisinopril 40mg tablet ◀                            │     │
│             │ Lisinopril 2.5mg/5mL solution                         │     │
│             └────────────────────────────────────────────────────────┘     │
│                                                                             │
│ ┌─ Order Details ───────────────────────────────────────────────────────┐  │
│ │                                                                       │  │
│ │ Dose:      [40    ] [mg ▼]                                           │  │
│ │ Route:     [Oral ▼]                                                  │  │
│ │ Frequency: [Once daily ▼]                                            │  │
│ │ Duration:  [90] days                                                 │  │
│ │                                                                       │  │
│ │ SIG: Take 1 tablet by mouth once daily                               │  │
│ │      [Edit SIG]                                                      │  │
│ │                                                                       │  │
│ │ Quantity:  [90  ]  tablets                                           │  │
│ │ Refills:   [3   ]                                                    │  │
│ │ DAW:       [ ] Dispense as written                                   │  │
│ │                                                                       │  │
│ │ Indication: [I10 - Essential hypertension ▼]                         │  │
│ │                                                                       │  │
│ │ Pharmacy:  [CVS - 123 Main St, Springfield ▼]                        │  │
│ │            [Change] [Patient Preferred]                              │  │
│ │                                                                       │  │
│ │ Notes to Pharmacist: [                                            ]  │  │
│ │                                                                       │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
│ ┌─ Alerts ──────────────────────────────────────────────────────────────┐  │
│ │ ✓ No drug interactions detected                                      │  │
│ │ ✓ No allergy conflicts                                               │  │
│ │ ⚠️ Duplicate therapy: Patient already on ACE inhibitor (Lisinopril   │  │
│ │    20mg). This will replace existing order.                          │  │
│ │    [Acknowledge]                                                     │  │
│ └───────────────────────────────────────────────────────────────────────┘  │
│                                                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                              [Cancel]  [Save to Orders]  [E-Prescribe Now] │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. Messages & Communication

### 5.1 Message Center

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ ✉️ Messages                                              [+ New Message]    │
├─────────────────────────────────────────────────────────────────────────────┤
│ Folders:              │  Inbox (12)                                        │
│ ┌──────────────────┐  │  ┌───────────────────────────────────────────────┐ │
│ │ 📥 Inbox (12)    │  │  │ ☐ │ 🔵 │ Smith, John        │ Refill request │ │
│ │ 📤 Sent          │  │  │   │    │ Requesting refill for metformin...  │ │
│ │ 📝 Drafts (2)    │  │  │   │    │ Today 9:15 AM                       │ │
│ │ 🗑️ Trash         │  │  │ ──┼────┼───────────────────────────────────── │ │
│ │                  │  │  │ ☐ │ 🟡 │ Garcia, Maria      │ Question about │ │
│ │ Labels:          │  │  │   │    │ diet                                │ │
│ │ 🏷️ Urgent        │  │  │   │    │ Yesterday 3:42 PM                   │ │
│ │ 🏷️ Refills       │  │  │ ──┼────┼───────────────────────────────────── │ │
│ │ 🏷️ Lab Questions │  │  │ ☐ │ ⚪ │ Cardiology Dept    │ Consult note   │ │
│ │ 🏷️ Referrals     │  │  │   │    │ for Wilson, Mary                    │ │
│ └──────────────────┘  │  │   │    │ Yesterday 11:20 AM                  │ │
│                       │  │ ──┼────┼───────────────────────────────────── │ │
│                       │  │ ☐ │ ⚪ │ Lab Department     │ Critical value │ │
│                       │  │   │    │ callback - Davis, Robert            │ │
│                       │  │   │    │ 01/17/2024 4:55 PM                  │ │
│                       │  └───────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 6. Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl/Cmd + K` | Global search |
| `Ctrl/Cmd + N` | New note |
| `Ctrl/Cmd + O` | New order |
| `Ctrl/Cmd + M` | New message |
| `Ctrl/Cmd + S` | Save |
| `Ctrl/Cmd + Enter` | Sign note |
| `Ctrl/Cmd + P` | Print |
| `Ctrl/Cmd + /` | Show shortcuts |
| `Esc` | Close modal/cancel |
| `Alt + 1-9` | Navigate chart sections |
| `.` + text | Trigger SmartText |

---

## 7. Color Coding & Visual Language

### Status Colors
- 🔴 **Red**: Critical, abnormal high, overdue, error
- 🟡 **Yellow/Amber**: Warning, abnormal, attention needed
- 🟢 **Green**: Normal, completed, success
- 🔵 **Blue**: In progress, informational, new
- ⚪ **Gray**: Inactive, historical, disabled

### Priority Indicators
- 🔴 Critical/STAT
- 🟠 Urgent
- 🟡 High
- ⚪ Routine

### Alert Severity
- 🚨 **Hard Stop**: Must acknowledge, cannot override
- ⚠️ **Warning**: Should acknowledge, can override with reason
- ℹ️ **Info**: Informational, no action required

---

## 8. Responsive Behavior

### Breakpoints
- **Desktop**: 1200px+ (full layout)
- **Tablet**: 768px-1199px (collapsible sidebar)
- **Mobile**: <768px (stacked layout, limited features)

### Mobile Considerations
- Patient lookup and basic chart view
- Vital signs entry
- Quick note entry
- Message reading/sending
- Schedule viewing
- Result review

---

## 9. Accessibility Requirements

- WCAG 2.1 AA compliance
- Minimum contrast ratio 4.5:1
- Focus indicators on all interactive elements
- Skip navigation links
- ARIA labels on icons and controls
- Screen reader announcements for dynamic content
- Keyboard navigation for all functions
- No reliance on color alone for information

---

## 10. Performance Targets

| Metric | Target |
|--------|--------|
| Initial page load | < 2 seconds |
| Chart open | < 1 second |
| Search results | < 500ms |
| Save operations | < 1 second |
| Note auto-save | Every 30 seconds |
| Session timeout warning | 5 minutes before |
| Session timeout | 15 minutes idle |
