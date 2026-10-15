package com.acme.salary.seed;

import java.util.List;
import java.util.Map;

/** ASCII-only name pools per region so generated emails are valid and readable. */
final class SeedNames {

    record Pool(List<String> female, List<String> male, List<String> last) {
    }

    private static List<String> l(String csv) {
        return List.of(csv.split(","));
    }

    private static final Pool ANGLO = new Pool(
            l("Emma,Olivia,Sophia,Isabella,Charlotte,Amelia,Mia,Harper,Evelyn,Abigail,Emily,Ella,Elizabeth,Sofia,Avery,Scarlett,Grace,Chloe,Victoria,Riley,Aria,Lily,Hannah,Natalie,Zoe,Nora,Hazel,Violet,Claire,Lucy"),
            l("Liam,Noah,Oliver,James,Elijah,William,Henry,Lucas,Benjamin,Theodore,Jack,Levi,Alexander,Mason,Ethan,Daniel,Jacob,Michael,Logan,Jackson,Sebastian,Owen,Samuel,Matthew,Joseph,David,Carter,Wyatt,Luke,Ryan"),
            l("Smith,Johnson,Williams,Brown,Jones,Miller,Davis,Wilson,Anderson,Taylor,Thomas,Moore,Martin,Jackson,Thompson,White,Harris,Clark,Lewis,Robinson,Walker,Young,Allen,King,Wright,Scott,Hill,Green,Adams,Baker,Nelson,Carter,Mitchell,Roberts,Turner,Phillips,Campbell,Parker,Evans,Edwards,Collins,Stewart,Morris,Murphy,Cook,Rogers,Morgan,Cooper,Peterson,Reed"));
    private static final Pool GERMAN = new Pool(
            l("Anna,Marie,Sophie,Lena,Laura,Julia,Hannah,Clara,Lea,Emilia,Johanna,Katharina,Greta,Ingrid,Petra,Sabine,Monika,Birgit,Heike,Franziska"),
            l("Lukas,Felix,Jonas,Leon,Maximilian,Paul,Tim,Jan,Stefan,Markus,Andreas,Thomas,Michael,Matthias,Florian,Klaus,Jurgen,Sebastian,Moritz,Tobias"),
            l("Muller,Schmidt,Schneider,Fischer,Weber,Meyer,Wagner,Becker,Schulz,Hoffmann,Koch,Richter,Klein,Wolf,Schroeder,Neumann,Schwarz,Zimmermann,Braun,Krueger,Hofmann,Hartmann,Lange,Werner,Krause,Lehmann,Keller,Vogel,Frank,Berger"));
    private static final Pool FRENCH = new Pool(
            l("Camille,Louise,Chloe,Manon,Lea,Emma,Jade,Alice,Ines,Juliette,Margaux,Claire,Sophie,Isabelle,Nathalie,Celine,Aurelie,Marion,Pauline,Elodie"),
            l("Louis,Gabriel,Jules,Hugo,Arthur,Lucas,Adrien,Antoine,Nicolas,Julien,Thomas,Pierre,Mathieu,Olivier,Laurent,Etienne,Maxime,Romain,Baptiste,Clement"),
            l("Martin,Bernard,Dubois,Thomas,Robert,Richard,Petit,Durand,Leroy,Moreau,Simon,Laurent,Lefebvre,Michel,Garcia,David,Bertrand,Roux,Vincent,Fournier,Morel,Girard,Andre,Mercier,Dupont,Lambert,Bonnet,Francois,Martinez,Legrand"));
    private static final Pool INDIAN = new Pool(
            l("Priya,Ananya,Diya,Aadhya,Saanvi,Kavya,Meera,Neha,Pooja,Riya,Sneha,Isha,Anjali,Divya,Shreya,Lakshmi,Nisha,Swati,Deepa,Radhika"),
            l("Arjun,Rohan,Vikram,Aditya,Karan,Rahul,Amit,Sanjay,Rajesh,Vivek,Siddharth,Ankit,Nikhil,Manish,Suresh,Pranav,Harsh,Varun,Gaurav,Ashwin"),
            l("Sharma,Patel,Singh,Kumar,Gupta,Reddy,Iyer,Nair,Mehta,Joshi,Verma,Rao,Desai,Kapoor,Chopra,Bose,Menon,Agarwal,Banerjee,Pillai,Shah,Malhotra,Chatterjee,Kulkarni,Bhatt,Saxena,Trivedi,Naidu,Mishra,Jain"));
    private static final Pool JAPANESE = new Pool(
            l("Yui,Hina,Sakura,Aoi,Rin,Mio,Yuna,Haruka,Akari,Misaki,Nanami,Kaori,Emi,Yoko,Keiko,Naoko,Ayumi,Mai,Risa,Saki"),
            l("Haruto,Sota,Ren,Yuto,Kaito,Takumi,Daiki,Kenji,Hiroshi,Takashi,Satoshi,Kazuki,Ryota,Shota,Naoki,Yusuke,Masato,Koji,Taro,Hideo"),
            l("Sato,Suzuki,Takahashi,Tanaka,Watanabe,Ito,Yamamoto,Nakamura,Kobayashi,Kato,Yoshida,Yamada,Sasaki,Yamaguchi,Matsumoto,Inoue,Kimura,Hayashi,Shimizu,Saito,Mori,Abe,Ikeda,Hashimoto,Ogawa,Fujita,Okada,Goto,Maeda,Hasegawa"));
    private static final Pool BRAZILIAN = new Pool(
            l("Maria,Ana,Beatriz,Julia,Larissa,Camila,Fernanda,Gabriela,Mariana,Isabela,Leticia,Carolina,Amanda,Bruna,Patricia,Renata,Vanessa,Aline,Luciana,Priscila"),
            l("Joao,Pedro,Lucas,Gabriel,Rafael,Matheus,Gustavo,Felipe,Bruno,Thiago,Rodrigo,Carlos,Eduardo,Leonardo,Diego,Marcelo,Fabio,Ricardo,Andre,Caio"),
            l("Silva,Santos,Oliveira,Souza,Rodrigues,Ferreira,Alves,Pereira,Lima,Gomes,Costa,Ribeiro,Martins,Carvalho,Almeida,Lopes,Soares,Fernandes,Vieira,Barbosa,Rocha,Dias,Nascimento,Andrade,Moreira,Nunes,Marques,Machado,Mendes,Freitas"));
    private static final Pool SINGAPOREAN = new Pool(
            l("Wei Ling,Mei,Xin Yi,Hui Min,Li Na,Siti,Nurul,Aisha,Priya,Shu Fen,Jia Hui,Ying,Rachel,Michelle,Jasmine,Cheryl,Sharon,Valerie,Joanne,Stephanie"),
            l("Wei Jie,Jun Hao,Zhi Wei,Kai Xiang,Ming,Ahmad,Hafiz,Raj,Kumar,Jia Le,Daryl,Brandon,Kelvin,Marcus,Darren,Alvin,Jason,Eugene,Ryan,Justin"),
            l("Tan,Lim,Lee,Ng,Ong,Wong,Goh,Chua,Chan,Koh,Teo,Ang,Yeo,Tay,Ho,Low,Sim,Chong,Abdullah,Rahman,Singh,Nair,Pillai,Chen,Liu,Yap,Foo,Toh,Seah,Lau"));

    private static final Map<String, Pool> BY_COUNTRY = Map.of(
            "US", ANGLO, "GB", ANGLO, "CA", ANGLO, "AU", ANGLO, "DE", GERMAN, "FR", FRENCH, "IN", INDIAN,
            "JP", JAPANESE, "BR", BRAZILIAN, "SG", SINGAPOREAN);

    private SeedNames() {
    }

    static Pool forCountry(String code) {
        return BY_COUNTRY.get(code);
    }
}
