package com.example.data.local

object SeedData {

    fun getDefaultQuestions(): List<QuestionEntity> {
        return listOf(
            // SCIENCE & PHYSICS
            QuestionEntity(
                question = "What is the speed of light in vacuum approximately?",
                options = listOf("300,000 km/s", "150,000 km/s", "3,000 km/s", "1,000,000 km/s"),
                correctIndex = 0,
                explanation = "The speed of light in a vacuum is exactly 299,792,458 m/s, approximately 300,000 km/s.",
                category = "Science",
                topic = "Physics",
                difficulty = 3,
                qualityScore = 0.98f,
                sourceReference = "NIST Fundamental Constants"
            ),
            QuestionEntity(
                question = "Which elementary particle carries a negative electric charge?",
                options = listOf("Proton", "Neutron", "Electron", "Positron"),
                correctIndex = 2,
                explanation = "Electrons carry a negative elementary electric charge, whereas protons carry positive charge and neutrons are neutral.",
                category = "Science",
                topic = "Atomic Physics",
                difficulty = 2,
                qualityScore = 0.99f,
                sourceReference = "CERN Particle Physics Compendium"
            ),
            QuestionEntity(
                question = "What fundamental force prevents atomic nuclei from flying apart due to electrostatic repulsion?",
                options = listOf("Gravitational Force", "Strong Nuclear Force", "Weak Nuclear Force", "Electromagnetic Force"),
                correctIndex = 1,
                explanation = "The strong nuclear force binds quarks together into hadrons and holds protons and neutrons together in atomic nuclei.",
                category = "Science",
                topic = "Nuclear Physics",
                difficulty = 6,
                qualityScore = 0.96f,
                sourceReference = "Nuclear Physics Fundamentals"
            ),
            QuestionEntity(
                question = "What process powers the core of our Sun?",
                options = listOf("Nuclear Fission", "Nuclear Fusion", "Chemical Combustion", "Gravitational Collapse"),
                correctIndex = 1,
                explanation = "The Sun converts hydrogen nuclei into helium through thermonuclear fusion, releasing immense energy.",
                category = "Science",
                topic = "Astrophysics",
                difficulty = 3,
                qualityScore = 0.97f,
                sourceReference = "NASA Solar Physics"
            ),
            QuestionEntity(
                question = "Which chemical element has the highest electrical conductivity at room temperature?",
                options = listOf("Copper", "Silver", "Gold", "Aluminum"),
                correctIndex = 1,
                explanation = "Silver (Ag) has the highest electrical and thermal conductivity of any known metal, though copper is more commonly used due to cost.",
                category = "Science",
                topic = "Chemistry",
                difficulty = 5,
                qualityScore = 0.95f,
                sourceReference = "CRC Handbook of Chemistry and Physics"
            ),

            // SPACE & ASTRONOMY
            QuestionEntity(
                question = "What is the largest moon of Saturn, known for having a dense atmosphere and liquid methane lakes?",
                options = listOf("Europa", "Titan", "Ganymede", "Enceladus"),
                correctIndex = 1,
                explanation = "Titan is Saturn's largest moon and the only moon in our solar system with a dense nitrogen atmosphere and liquid hydrocarbon lakes.",
                category = "Space",
                topic = "Planetary Science",
                difficulty = 4,
                qualityScore = 0.98f,
                sourceReference = "Cassini-Huygens Mission Data"
            ),
            QuestionEntity(
                question = "What is the name of the boundary around a black hole beyond which nothing can escape?",
                options = listOf("Accretion Disk", "Event Horizon", "Ergosphere", "Photon Sphere"),
                correctIndex = 1,
                explanation = "The event horizon is the threshold where the gravitational pull becomes so strong that escape velocity exceeds the speed of light.",
                category = "Space",
                topic = "Astrophysics",
                difficulty = 4,
                qualityScore = 0.97f,
                sourceReference = "General Relativity Concepts"
            ),
            QuestionEntity(
                question = "Which space telescope, launched in December 2021, observes the universe primarily in the infrared spectrum?",
                options = listOf("Hubble Space Telescope", "James Webb Space Telescope", "Spitzer Space Telescope", "Chandra Observatory"),
                correctIndex = 1,
                explanation = "The James Webb Space Telescope (JWST) uses high-resolution infrared detectors to peer into the earliest galaxies.",
                category = "Space",
                topic = "Space Telescopes",
                difficulty = 3,
                qualityScore = 0.99f,
                sourceReference = "ESA/NASA JWST Mission"
            ),
            QuestionEntity(
                question = "Which planet in our solar system has the highest surface temperature due to an extreme runaway greenhouse effect?",
                options = listOf("Mercury", "Venus", "Mars", "Jupiter"),
                correctIndex = 1,
                explanation = "Despite Mercury being closer to the Sun, Venus has a thick CO2 atmosphere creating average surface temperatures of around 465°C (870°F).",
                category = "Space",
                topic = "Solar System",
                difficulty = 4,
                qualityScore = 0.96f,
                sourceReference = "NASA Planetary Fact Sheet"
            ),

            // TECHNOLOGY & COMPUTING
            QuestionEntity(
                question = "In algorithmic complexity, what does O(log n) time complexity represent?",
                options = listOf("Constant Time", "Linear Time", "Logarithmic Time", "Quadratic Time"),
                correctIndex = 2,
                explanation = "O(log n) indicates logarithmic time complexity, commonly found in balanced binary search algorithms.",
                category = "Technology",
                topic = "Computer Science",
                difficulty = 5,
                qualityScore = 0.97f,
                sourceReference = "Introduction to Algorithms (CLRS)"
            ),
            QuestionEntity(
                question = "Which programming language was developed by JetBrains and is the preferred language for modern Android development?",
                options = listOf("Java", "Kotlin", "Dart", "Swift"),
                correctIndex = 1,
                explanation = "Kotlin was developed by JetBrains and officially declared Google's preferred language for Android in 2019.",
                category = "Technology",
                topic = "Software Engineering",
                difficulty = 2,
                qualityScore = 0.99f,
                sourceReference = "Android Developers Official Guide"
            ),
            QuestionEntity(
                question = "What does the acronym 'GPU' stand for in computer hardware?",
                options = listOf("General Processing Unit", "Graphics Processing Unit", "Gaming Performance Unit", "Grid Power Unit"),
                correctIndex = 1,
                explanation = "GPU stands for Graphics Processing Unit, specialized electronic circuits for parallel image rendering and neural network computation.",
                category = "Technology",
                topic = "Computer Architecture",
                difficulty = 2,
                qualityScore = 0.98f,
                sourceReference = "IEEE Hardware Glossary"
            ),
            QuestionEntity(
                question = "What cryptographic mechanism is primarily used to ensure data integrity and authenticity in blockchain networks?",
                options = listOf("Symmetric AES encryption", "Cryptographic hash functions & digital signatures", "Caesar ciphers", "Huffman coding"),
                correctIndex = 1,
                explanation = "Blockchains utilize cryptographic hashing (like SHA-256) and asymmetric public-key digital signatures for verification.",
                category = "Technology",
                topic = "Cryptography",
                difficulty = 6,
                qualityScore = 0.95f,
                sourceReference = "NIST Cryptographic Standards"
            ),
            QuestionEntity(
                question = "In machine learning, what does 'RAG' stand for?",
                options = listOf("Recursive Auto Generation", "Retrieval-Augmented Generation", "Randomized Array Gradient", "Regressive Adaptive Gradient"),
                correctIndex = 1,
                explanation = "Retrieval-Augmented Generation (RAG) grounds language models on external verified knowledge corpora to prevent hallucination.",
                category = "Technology",
                topic = "Artificial Intelligence",
                difficulty = 4,
                qualityScore = 0.99f,
                sourceReference = "NeurIPS AI Papers"
            ),

            // WORLD HISTORY
            QuestionEntity(
                question = "In which year did the Apollo 11 mission successfully land the first humans on the Moon?",
                options = listOf("1965", "1969", "1972", "1961"),
                correctIndex = 1,
                explanation = "Neil Armstrong and Buzz Aldrin landed on the Lunar surface on July 20, 1969.",
                category = "History",
                topic = "20th Century History",
                difficulty = 3,
                qualityScore = 0.99f,
                sourceReference = "NASA Historical Archives"
            ),
            QuestionEntity(
                question = "Who was the first Emperor of a unified China, famous for commissioning the Terracotta Army?",
                options = listOf("Qin Shi Huang", "Han Wudi", "Kublai Khan", "Sun Yat-sen"),
                correctIndex = 0,
                explanation = "Qin Shi Huang unified the Warring States in 221 BC and established the Qin dynasty.",
                category = "History",
                topic = "Ancient History",
                difficulty = 5,
                qualityScore = 0.96f,
                sourceReference = "Records of the Grand Historian"
            ),
            QuestionEntity(
                question = "The ancient Library of Alexandria was located in which modern country?",
                options = listOf("Greece", "Egypt", "Turkey", "Italy"),
                correctIndex = 1,
                explanation = "The Library of Alexandria was one of the largest and most significant libraries of the ancient world, located in Alexandria, Egypt.",
                category = "History",
                topic = "Hellenistic World",
                difficulty = 3,
                qualityScore = 0.98f,
                sourceReference = "Encyclopedia of Ancient History"
            ),
            QuestionEntity(
                question = "Which peace treaty, signed in 1648, established the concept of modern state sovereignty in Europe?",
                options = listOf("Treaty of Versailles", "Peace of Westphalia", "Treaty of Utrecht", "Congress of Vienna"),
                correctIndex = 1,
                explanation = "The Peace of Westphalia ended the Thirty Years' War and Eighty Years' War, creating the Westphalian sovereignty model.",
                category = "History",
                topic = "European History",
                difficulty = 7,
                qualityScore = 0.94f,
                sourceReference = "Cambridge Modern History"
            ),

            // GEOGRAPHY
            QuestionEntity(
                question = "Which is the longest river in the world by general consensus?",
                options = listOf("Amazon River", "Nile River", "Yangtze River", "Mississippi River"),
                correctIndex = 1,
                explanation = "The Nile River in northeastern Africa flows approximately 6,650 km (4,132 miles), traditionally ranked as the longest river.",
                category = "Geography",
                topic = "Physical Geography",
                difficulty = 2,
                qualityScore = 0.97f,
                sourceReference = "National Geographic Hydrography"
            ),
            QuestionEntity(
                question = "What is the capital city of Australia?",
                options = listOf("Sydney", "Melbourne", "Canberra", "Brisbane"),
                correctIndex = 2,
                explanation = "Canberra was chosen as the compromise capital of Australia in 1908 between rivals Sydney and Melbourne.",
                category = "Geography",
                topic = "World Capitals",
                difficulty = 3,
                qualityScore = 0.99f,
                sourceReference = "Geographic Names Database"
            ),
            QuestionEntity(
                question = "Which desert is the largest hot desert on Earth?",
                options = listOf("Gobi Desert", "Kalahari Desert", "Sahara Desert", "Arabian Desert"),
                correctIndex = 2,
                explanation = "The Sahara covers over 9 million square kilometers across North Africa, making it the largest non-polar desert on Earth.",
                category = "Geography",
                topic = "Landforms",
                difficulty = 2,
                qualityScore = 0.98f,
                sourceReference = "World Landforms Atlas"
            ),
            QuestionEntity(
                question = "Which strait connects the Mediterranean Sea to the Atlantic Ocean?",
                options = listOf("Strait of Hormuz", "Strait of Gibraltar", "Bosphorus Strait", "Strait of Malacca"),
                correctIndex = 1,
                explanation = "The Strait of Gibraltar connects the Atlantic Ocean to the Mediterranean Sea and separates Spain from Morocco.",
                category = "Geography",
                topic = "Maritime Geography",
                difficulty = 4,
                qualityScore = 0.97f,
                sourceReference = "International Hydrographic Organization"
            ),

            // NATURE & BIOLOGY
            QuestionEntity(
                question = "What is the primary photosynthetic pigment in plants that absorbs blue and red light while reflecting green light?",
                options = listOf("Carotenoid", "Chlorophyll a", "Anthocyanin", "Xanthophyll"),
                correctIndex = 1,
                explanation = "Chlorophyll is the green pigment in chloroplasts essential for oxygenic photosynthesis.",
                category = "Nature",
                topic = "Plant Biology",
                difficulty = 3,
                qualityScore = 0.98f,
                sourceReference = "Campbell Biology"
            ),
            QuestionEntity(
                question = "Which mammal is known to be the only one capable of sustained flapping flight?",
                options = listOf("Flying Squirrel", "Bat", "Sugar Glider", "Colugo"),
                correctIndex = 1,
                explanation = "Bats (order Chiroptera) are the only mammals with true sustained powered flight; others only glide.",
                category = "Nature",
                topic = "Zoology",
                difficulty = 3,
                qualityScore = 0.98f,
                sourceReference = "Mammalogy Taxonomic Reference"
            ),
            QuestionEntity(
                question = "What is the largest living species of reptile on Earth?",
                options = listOf("Komodo Dragon", "Saltwater Crocodile", "Green Anaconda", "Leatherback Sea Turtle"),
                correctIndex = 1,
                explanation = "The Saltwater Crocodile (Crocodylus porosus) can exceed 6 meters (20 feet) in length and weigh over 1,000 kg.",
                category = "Nature",
                topic = "Herpetology",
                difficulty = 4,
                qualityScore = 0.96f,
                sourceReference = "IUCN Crocodile Specialist Group"
            ),

            // SPORTS & ESPORTS
            QuestionEntity(
                question = "In the game of chess, what is the maximum number of squares a Knight can attack from the center of an empty board?",
                options = listOf("4", "6", "8", "10"),
                correctIndex = 2,
                explanation = "From any central square (d4, d5, e4, e5), a knight controls exactly 8 unique squares in an L-shaped move.",
                category = "Sports",
                topic = "Chess Strategy",
                difficulty = 3,
                qualityScore = 0.99f,
                sourceReference = "FIDE Chess Handbook"
            ),
            QuestionEntity(
                question = "How many players are on the field for one team during a standard regulation soccer (association football) match?",
                options = listOf("9", "10", "11", "12"),
                correctIndex = 2,
                explanation = "Each team consists of 11 players including 1 goalkeeper on the field.",
                category = "Sports",
                topic = "Football",
                difficulty = 1,
                qualityScore = 0.99f,
                sourceReference = "IFAB Laws of the Game"
            ),
            QuestionEntity(
                question = "How many rings are featured on the official Olympic flag?",
                options = listOf("4", "5", "6", "7"),
                correctIndex = 1,
                explanation = "The five interlocking rings (blue, yellow, black, green, red) represent the five inhabited continents united in competition.",
                category = "Sports",
                topic = "Olympic Games",
                difficulty = 2,
                qualityScore = 0.99f,
                sourceReference = "International Olympic Committee"
            ),

            // LITERATURE & ARTS
            QuestionEntity(
                question = "Who wrote the classic dystopian novel '1984'?",
                options = listOf("Aldous Huxley", "George Orwell", "Ray Bradbury", "Philip K. Dick"),
                correctIndex = 1,
                explanation = "George Orwell (Eric Arthur Blair) published '1984' in 1949, examining totalitarianism, surveillance, and Doublethink.",
                category = "Literature",
                topic = "Modern Literature",
                difficulty = 2,
                qualityScore = 0.99f,
                sourceReference = "Oxford Companion to English Literature"
            ),
            QuestionEntity(
                question = "Which Renaissance master painted the ceiling of the Sistine Chapel in Vatican City?",
                options = listOf("Leonardo da Vinci", "Michelangelo", "Raphael", "Donatello"),
                correctIndex = 1,
                explanation = "Michelangelo Buonarroti painted the Sistine Chapel ceiling between 1508 and 1512 under the patronage of Pope Julius II.",
                category = "Literature",
                topic = "Art History",
                difficulty = 3,
                qualityScore = 0.98f,
                sourceReference = "Vatican Museum Archives"
            ),

            // MATHEMATICS
            QuestionEntity(
                question = "What is the smallest prime number?",
                options = listOf("0", "1", "2", "3"),
                correctIndex = 2,
                explanation = "2 is the smallest prime number and the only even prime number in mathematics.",
                category = "Mathematics",
                topic = "Number Theory",
                difficulty = 2,
                qualityScore = 0.99f,
                sourceReference = "Elementary Number Theory"
            ),
            QuestionEntity(
                question = "In Euclidean geometry, what is the sum of interior angles in any planar triangle?",
                options = listOf("90 degrees", "180 degrees", "270 degrees", "360 degrees"),
                correctIndex = 1,
                explanation = "In standard Euclidean planar geometry, the interior angles of any triangle always sum to 180° (π radians).",
                category = "Mathematics",
                topic = "Geometry",
                difficulty = 2,
                qualityScore = 0.99f,
                sourceReference = "Euclid's Elements"
            ),
            QuestionEntity(
                question = "What is the value of the golden ratio (Phi) approximately?",
                options = listOf("1.414", "1.618", "2.718", "3.141"),
                correctIndex = 1,
                explanation = "The golden ratio (1 + √5)/2 is approximately 1.6180339887...",
                category = "Mathematics",
                topic = "Algebra",
                difficulty = 4,
                qualityScore = 0.97f,
                sourceReference = "Mathematical Constants Handbook"
            )
        )
    }

    fun getDefaultQuests(): List<QuestEntity> {
        return listOf(
            QuestEntity(
                id = "quest_duel_3",
                title = "Duel Challenger",
                description = "Complete 3 Quick Duels in the Arena",
                currentProgress = 1,
                targetProgress = 3,
                xpReward = 150,
                coinReward = 50,
                category = "Daily"
            ),
            QuestEntity(
                id = "quest_win_2",
                title = "Victory March",
                description = "Win 2 competitive matches with >70% accuracy",
                currentProgress = 1,
                targetProgress = 2,
                xpReward = 200,
                coinReward = 80,
                category = "Daily"
            ),
            QuestEntity(
                id = "quest_science_5",
                title = "Science Savant",
                description = "Answer 5 Science & Tech questions correctly",
                currentProgress = 3,
                targetProgress = 5,
                xpReward = 120,
                coinReward = 40,
                category = "Daily"
            ),
            QuestEntity(
                id = "quest_streak_perfect",
                title = "Flawless Instinct",
                description = "Achieve a 4-question combo streak",
                currentProgress = 3,
                targetProgress = 4,
                xpReward = 180,
                coinReward = 60,
                category = "Daily"
            )
        )
    }

    fun getDefaultAchievements(): List<AchievementEntity> {
        return listOf(
            AchievementEntity(
                id = "ach_first_win",
                title = "First Blood",
                description = "Win your first 1v1 Quick Duel",
                iconName = "trophy",
                isUnlocked = true,
                progress = 1,
                maxProgress = 1,
                unlockedAt = System.currentTimeMillis() - 86400000L,
                rewardXp = 100
            ),
            AchievementEntity(
                id = "ach_silver_league",
                title = "Ascending the Ranks",
                description = "Reach the Silver Competitive League",
                iconName = "shield",
                isUnlocked = true,
                progress = 1,
                maxProgress = 1,
                unlockedAt = System.currentTimeMillis() - 43200000L,
                rewardXp = 250
            ),
            AchievementEntity(
                id = "ach_speed_demon",
                title = "Lightning Reflexes",
                description = "Answer 10 questions in under 2 seconds correctly",
                iconName = "lightning",
                isUnlocked = false,
                progress = 6,
                maxProgress = 10,
                rewardXp = 300
            ),
            AchievementEntity(
                id = "ach_streak_7",
                title = "Flame Keeper",
                description = "Maintain a 7-day daily challenge streak",
                iconName = "fire",
                isUnlocked = false,
                progress = 3,
                maxProgress = 7,
                rewardXp = 500
            ),
            AchievementEntity(
                id = "ach_century",
                title = "Master Scholar",
                description = "Answer 100 total questions correctly across any mode",
                iconName = "star",
                isUnlocked = false,
                progress = 42,
                maxProgress = 100,
                rewardXp = 1000
            )
        )
    }

    fun getDefaultCosmetics(): List<CosmeticEntity> {
        return listOf(
            CosmeticEntity(
                id = "avatar_cyber_king",
                name = "Cyber Crown",
                type = "AVATAR",
                priceCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                icon = "crown",
                rarity = "COMMON"
            ),
            CosmeticEntity(
                id = "avatar_neon_falcon",
                name = "Neon Falcon",
                type = "AVATAR",
                priceCoins = 200,
                isUnlocked = true,
                isEquipped = false,
                icon = "falcon",
                rarity = "RARE"
            ),
            CosmeticEntity(
                id = "avatar_cosmic_sage",
                name = "Cosmic Sage",
                type = "AVATAR",
                priceCoins = 500,
                isUnlocked = false,
                isEquipped = false,
                icon = "sage",
                rarity = "EPIC"
            ),
            CosmeticEntity(
                id = "avatar_esports_titan",
                name = "Esports Titan",
                type = "AVATAR",
                priceCoins = 1000,
                isUnlocked = false,
                isEquipped = false,
                icon = "titan",
                rarity = "LEGENDARY"
            ),
            CosmeticEntity(
                id = "frame_cyan_neon",
                name = "Cyan Pulse Frame",
                type = "FRAME",
                priceCoins = 0,
                isUnlocked = true,
                isEquipped = true,
                icon = "frame_cyan",
                rarity = "COMMON"
            ),
            CosmeticEntity(
                id = "frame_gold_champion",
                name = "Aegis of Gold",
                type = "FRAME",
                priceCoins = 350,
                isUnlocked = false,
                isEquipped = false,
                icon = "frame_gold",
                rarity = "RARE"
            ),
            CosmeticEntity(
                id = "frame_diamond_glitch",
                name = "Diamond Overdrive",
                type = "FRAME",
                priceCoins = 800,
                isUnlocked = false,
                isEquipped = false,
                icon = "frame_diamond",
                rarity = "LEGENDARY"
            )
        )
    }
}
